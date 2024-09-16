package com.kzdrava.webapp.controllers;

import com.kzdrava.webapp.configs.Config;
import com.kzdrava.webapp.dto.CurrentFolder;
import com.kzdrava.webapp.dto.DataCollection;
import com.kzdrava.webapp.dto.FileDto;
import com.kzdrava.webapp.services.*;
import com.kzdrava.webapp.utils.ResourceUtils;
import com.kzdrava.webapp.utils.DataCollectionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.unipi.entities.CustomFile;
import org.unipi.entities.Folder;
import org.unipi.entities.ResourceEnum;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Controller
@RequestMapping("/data-collections")
@Slf4j(topic = "FileController")
@SessionAttributes({"currentFolder", "username"})
@RequiredArgsConstructor
public class DataCollectionsController {

    private final FolderService folderService;
    private final FileService fileService;
    private final ResourceService resourceService;
    private final EmailService emailService;
    private final SharingCollectionsService sharingCollectionsService;
    

    @PostMapping("/new-file/{parentFolderId}")
    public String handleFileUpload(@RequestParam("files") MultipartFile file, @PathVariable String parentFolderId)  {
        final FileDto fileDto;
        try {
             fileDto = FileDto
                    .builder()
                    .name(file.getOriginalFilename())
                    .fileContent(file.getBytes())
                     .size(file.getSize())
                    .build();

            Folder folder = folderService.getFolderFromDB(parentFolderId);
            fileService.updateFile(fileDto, folder);
        }catch (IOException e) {
            log.error("File with name {} is not correct", file.getName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/data-collections/collections/" + parentFolderId;
    }

    @GetMapping("/collections")
    public String retrieveResourcesHandler(Model model, HttpSession session) throws NoSuchAlgorithmException {
        CurrentFolder currentFolder = getUserRootFolderCollections();
        List<org.unipi.entities.Resource> resources = sharingCollectionsService
                .retrieveSharedReceivedResources(Config.getUsername());
        List<DataCollection> receivedResources = retrievedCollectionsThatUserHasAccess(resources);
        currentFolder.setDataCollectionList(DataCollectionUtils
                .concatDataCollections(currentFolder.getDataCollectionList(),receivedResources ));
        model.addAttribute("currentFolder", currentFolder);
        session.setAttribute("currentFolder", currentFolder);
        return "home";
    }


    @GetMapping("/collections/{id}")
    public String getCollectionsOfFolder(@PathVariable String id, Model model, HttpSession session) {
        Folder folder = folderService.getFolderFromDB(id);
        CurrentFolder currentFolder;
        if(Objects.isNull(folder)) {
             currentFolder = new CurrentFolder(id,
                    Collections.emptyList());
        }else{
            List<org.unipi.entities.Resource> resources = folder.getSubResources();
            List<DataCollection> dataCollections =  DataCollectionUtils.mapResourcesToDataCollections(resources);
            currentFolder = new CurrentFolder(id,
                    dataCollections);
        }
        model.addAttribute("currentFolder", currentFolder);
        session.setAttribute("currentFolder", currentFolder);
        return "home";
    }

    //TODO if file was deleted you should not be able to download it and have to refresh the page
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadHandler(@PathVariable String id) {
        org.unipi.entities.Resource resource = resourceService.getResourceById(id);
        if(Objects.isNull(resource)) {
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                    .location(URI.create("/data-collections/collections")).build();
        }else if(resource.getResource().equals(ResourceEnum.FILE)) {
            CustomFile file = fileService.getFileFromDBById(resource.getId());
            byte[] chunkBlobs = fileService.constructFileContentFromChunks(file);
            return ResourceUtils.handleBrowserFiles( chunkBlobs, ContentDisposition.attachment()
                    .filename(resource.getName(), StandardCharsets.UTF_8).build());
        }
        return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                    .location(URI.create("/data-collections/collections")).build();

    }


    @PostMapping("/delete/{id}")
    public String deleteFileHandler(@PathVariable String id) {
         resourceService.deleteResourceById(id, Config.getUsername());
         return "redirect:/data-collections/collections";
    }


    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource>  previewHandler(@PathVariable String id) {
        CustomFile customFile = fileService.getFileFromDBById(id);
        byte[] fileContent = null;
        String filename = "Empty";
        if(Objects.nonNull(customFile)) {
            fileContent = fileService.constructFileContentFromChunks(customFile);
            filename = customFile.getName();
        }
        return ResourceUtils.handleBrowserFiles(fileContent, ContentDisposition
                .inline().filename(filename).build());
    }

    @PostMapping("/create-folder/{parentFolderId}")
    public String createFolderHandler(@RequestParam("name") String name,
                                      @PathVariable String parentFolderId, RedirectAttributes redirectAttributes)
            throws NoSuchAlgorithmException {
        Folder folder = folderService.getFolderFromDB(parentFolderId);
        ResponseEntity<String> response = folderService.saveFolder(name, folder, Config.getUsername());
        redirectAttributes.addFlashAttribute("statusCode", response.getStatusCode().value());
        redirectAttributes.addFlashAttribute("statusMessage",response.getBody());
        if(response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/data-collections/collections/" + parentFolderId;
        }
        return "redirect:/data-collections/collections";
    }

    @GetMapping("/my-collections")
    public String myCollectionsHandler(Model model, HttpSession httpSession) throws NoSuchAlgorithmException {
        CurrentFolder currentFolder = getUserRootFolderCollections();
        model.addAttribute("currentFolder", currentFolder);
        httpSession.setAttribute("currentFolder", currentFolder);
        return "home";
    }

    @GetMapping("/shared-collections")
    public String sharedByMeCollectionsHandler(Model model, HttpSession httpSession) throws NoSuchAlgorithmException {

        List<org.unipi.entities.Resource> resources = sharingCollectionsService
                .retrieveSharedResourcesByUser(Config.getUsername());
        CurrentFolder currentFolder;
        Folder folder =  folderService.getOrCreateRootFolder(Config.getUsername());
        if(resources.isEmpty()) {
            currentFolder = new CurrentFolder(folder.getId(), Collections.emptyList());
            model.addAttribute("currentFolder", currentFolder);
            httpSession.setAttribute("currentFolder", currentFolder);
        }else{
            List<DataCollection> sharedCollections = retrievedCollectionsThatUserHasAccess(resources);
            currentFolder = new CurrentFolder(folder.getId(), sharedCollections);
            model.addAttribute("currentFolder", currentFolder);
            httpSession.setAttribute("currentFolder", currentFolder);
        }
        return "home";
    }

    @GetMapping("/received-collections")
    public String receivedCollectionsHandler(Model model, HttpSession httpSession) {
        List<org.unipi.entities.Resource> resources = sharingCollectionsService
                .retrieveSharedReceivedResources(Config.getUsername());
        CurrentFolder currentFolder;
        if(resources.isEmpty()) {
            currentFolder = new CurrentFolder("", Collections.emptyList());
            model.addAttribute("currentFolder", currentFolder);
            httpSession.setAttribute("currentFolder", currentFolder);
        }else{
            List<DataCollection> receivedCollections = retrievedCollectionsThatUserHasAccess(resources);
            currentFolder = new CurrentFolder("", receivedCollections);
            model.addAttribute("currentFolder", currentFolder);
            httpSession.setAttribute("currentFolder", currentFolder);
        }
        return "home";
    }

    @GetMapping("/shared-resource/{id}")
    public String handleSharedResource(@PathVariable String id, Model model, HttpSession session) {
        ResponseEntity<String> response = sharingCollectionsService.findSharedResourcesForUser(id, Config.getUsername());
        CurrentFolder currentFolder;
        if(response.getStatusCode().is4xxClientError()) {
             currentFolder = new CurrentFolder(id,
                    Collections.emptyList());
            model.addAttribute("currentFolder", currentFolder);
            model.addAttribute("resourceMessage", response.getBody());
            session.setAttribute("currentFolder", currentFolder);
        }else{
            org.unipi.entities.Resource resource = resourceService.getResourceById(id);
            resource = resource.getResource().equals(ResourceEnum.FOLDER)
                    ? resource : fileService.getFileFromDBById(id);
            List<DataCollection> dataCollections = DataCollectionUtils.mapResourcesToDataCollections(List.of(resource));
            currentFolder = new CurrentFolder(id,
                    dataCollections);
            model.addAttribute("currentFolder", currentFolder);
            session.setAttribute("currentFolder", currentFolder);
        }
        return "home";
    }


    private CurrentFolder getUserRootFolderCollections() throws NoSuchAlgorithmException {
        Folder folder =  folderService.getOrCreateRootFolder(Config.getUsername());
        List<org.unipi.entities.Resource> resources = folder.getSubResources();
        List<DataCollection> dataCollections = DataCollectionUtils.mapResourcesToDataCollections(resources);
        return new CurrentFolder(folder.getId(), dataCollections);
    }

    @ModelAttribute("dataCollection")
    public DataCollection dataCollectionModelAttribute() {
        return new DataCollection();
    }


    private List<DataCollection> retrievedCollectionsThatUserHasAccess(List<org.unipi.entities.Resource> resources) {

        List<String> fileIds = DataCollectionUtils.getFilesIdsFromResources(resources);
        List<String> folderIds = DataCollectionUtils.getFoldersIdsFromResources(resources);

        List<DataCollection> folderCollections = DataCollectionUtils.mapFoldersToDataCollections(
                folderService.getFoldersByIds(folderIds)
        );
        List<DataCollection> fileCollections = DataCollectionUtils.mapFilesToDataCollections(
                fileService.getFilesByIds(fileIds)
        );
        return DataCollectionUtils.concatDataCollections(folderCollections, fileCollections);

    }
    @ModelAttribute("username")
    public String currentUser() {
        String username = Config.getUsername();
        return (username == null || username.trim().isEmpty()) ? "Unknown" : username;
    }

}
