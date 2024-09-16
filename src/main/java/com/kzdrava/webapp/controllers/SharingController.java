package com.kzdrava.webapp.controllers;


import com.kzdrava.webapp.configs.Config;
import com.kzdrava.webapp.dto.PendingShare;
import com.kzdrava.webapp.dto.UserShareRevoker;
import com.kzdrava.webapp.services.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.unipi.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shares")
@Slf4j(topic = "SharingController")
@RequiredArgsConstructor
public class SharingController {

    private final SharingCollectionsService sharingCollectionsService;
    private final EmailService emailService;
    private final ResourceService resourceService;

    @GetMapping("/accept/{id}/{user}")
    public String acceptResourceHandler(@PathVariable String id) {
        sharingCollectionsService.updateSharingResourceStatus(id);
        return "redirect:/data-collections/collections";
    }

    @GetMapping("/reject/{id}/{receiver}")
    public String rejectResourceHandler(@PathVariable String id, @PathVariable String receiver) {
        sharingCollectionsService.rejectSharingRequestForResource(id, receiver);
        return "redirect:/data-collections/collections";
    }

//    @PostMapping("/share/{id}")
//    public String shareResourceHandler(@PathVariable String id, @RequestParam("email") String email,
//                                   @RequestParam("isFolder") boolean isFolder) throws MessagingException {
//
//        if(isFolder) {
//            ClientFolder folder = dataCollectionService.extractFolderById(id);
//            if(Objects.nonNull(folder) && !folder.getOwner().equals("kzdrava50@gmail.com")) {
//                emailService.sendFolderEmail("kzdrava50@gmail.com",
//                        folder.getName(), OWNER, email, id);
//
//            }
//        }else {
//            ClientFile file = dataCollectionService.extractFileById(id);
//            if(Objects.nonNull(file) && !file.getOwner().equals("kzdrava50@gmail.com")) {
//                byte[] fileByteArray = dataCollectionService.constructFileContent(file.getId());
//                emailService.sendFileEmail("kzdrava50@gmail.com",
//                        file.getName(), fileByteArray, OWNER, email, id);
//                sharingCollectionsService.shareResource(file, email, OWNER);
//            }
//        }
//        return "redirect:/data-collections/collections";
//    }

    @PostMapping("/share/{id}")
    public String shareFileHandler(@RequestParam("email") Set<String> emails,
                                           @PathVariable String id, RedirectAttributes redirectAttributes) throws MessagingException {

        Resource resource = resourceService.getResourceById(id);
        if (Objects.isNull(resource)) {
            redirectAttributes.addFlashAttribute("statusCode", HttpStatus.BAD_REQUEST.value());
            redirectAttributes.addFlashAttribute( "statusMessage", "Resource with id: " + id + "is missing");
        } else if(emails.isEmpty()) {
            redirectAttributes.addFlashAttribute("statusCode", HttpStatus.BAD_REQUEST.value());
            redirectAttributes.addFlashAttribute("statusMessage", "You have to enter at least one email");
        }
        else {
           ResponseEntity<String> response = sharingCollectionsService
                   .shareResource(resource, emails, Config.getUsername());
           if(response.getStatusCode().is2xxSuccessful() && !resource.getOwner().equals(Config.getUsername())) {
               emailService.sendEmail(resource.getOwner(), resource.getName(), Config.getUsername(), emails);
           }
            redirectAttributes.addFlashAttribute("statusCode", response.getStatusCode().value());
            redirectAttributes.addFlashAttribute("statusMessage",response.getBody());
        }
        return "redirect:/data-collections/collections";
    }


    @PostMapping("/share-via-link/{id}")
    public String shareFileHandler(@PathVariable String id, RedirectAttributes redirectAttributes) throws MessagingException {

        Resource resource = resourceService.getResourceById(id);
        if (Objects.isNull(resource)) {
            redirectAttributes.addFlashAttribute("statusCode", HttpStatus.BAD_REQUEST.value());
            redirectAttributes.addFlashAttribute( "statusMessage", "Resource with id: " + id + "is missing");
        } else {
            ResponseEntity<String> response = sharingCollectionsService.shareResourceLink(resource, Config.getUsername());
            if(response.getStatusCode().is2xxSuccessful() && !resource.getOwner().equals(Config.getUsername())) {
                emailService.sendEmail(resource.getOwner(), resource.getName(),
                        Config.getUsername(),Set.of("all users") );
            }
            redirectAttributes.addFlashAttribute("statusCode", response.getStatusCode().value());
            redirectAttributes.addFlashAttribute("statusMessage",response.getBody());
        }
        return "redirect:/data-collections/collections";
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserShareRevoker> getUsersThatHaveAccessToResource(@PathVariable String id) {
        UserShareRevoker userShareRevoker = new UserShareRevoker(id, sharingCollectionsService
                .findUsersHavingAccessToResource(id));
        return ResponseEntity.ok().
                body(userShareRevoker);
    }

    @GetMapping("/revoke/{id}/{user}")
    public String revokeResourceShare(@PathVariable String id, @PathVariable String user) {
        sharingCollectionsService.revokeResourceSharingToUser(id, user);
        return "redirect:/data-collections/collections";
    }

    @GetMapping("/get-pending-shares")
    public ResponseEntity<List<PendingShare>> getPendingShares() {
        List<Share> shares = sharingCollectionsService.findOwnerPendingShares(Config.getUsername());

        List<PendingShare> pendingShares = shares
                .stream().map(share -> new PendingShare(share.getResource().getId(),
                        share.getResource().getName(), share.getResourceReceiver())).toList();
        pendingShares.forEach(pendingShare -> log.info("Share {}", pendingShare.getResourceName()));

        return ResponseEntity.ok().body(pendingShares);
    }

}
