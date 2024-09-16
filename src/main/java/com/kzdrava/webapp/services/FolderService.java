package com.kzdrava.webapp.services;

import com.kzdrava.webapp.configs.Config;
import com.kzdrava.webapp.utils.DataCollectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unipi.entities.Folder;
import org.unipi.repositories.FolderRepository;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kzdrava.webapp.constants.Constant.ROOT;
import static org.unipi.entities.ResourceEnum.FOLDER;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final SharingCollectionsService sharingCollectionsService;

    @Transactional
    public Folder getOrCreateRootFolder(String owner) throws NoSuchAlgorithmException {
        Folder rootFolder = folderRepository.findByOwnerAndParentFolderIsNull(owner);
        if(Objects.isNull(rootFolder)) {
            Folder folder = new Folder(ROOT, owner, FOLDER);
            folder.setId(DataCollectionUtils.calculateHash(folder));
            folder.setSubResources(new ArrayList<>());
            folderRepository.save(folder);
            return folder;
        }
        return rootFolder;
    }


    public Folder getFolderFromDB(String id) {
        return folderRepository
                .findById(id)
                .orElse(null);
    }

    public ResponseEntity<String> saveFolder(String name, Folder parentFolder , String owner) throws NoSuchAlgorithmException {
        Folder folder = new Folder(name, owner, FOLDER);
        folder.setParentFolder(parentFolder);
        folder.setId(DataCollectionUtils.calculateHash(folder));
        if(folderDoesNotExist(folder)) {
            folderRepository.save(folder);
            return ResponseEntity.ok().body("Folder created successfully");
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Folder already exists");
        }

    }

    public boolean folderDoesNotExist(Folder folder) {
        return !folderRepository.existsById(folder.getId());
    }


    public List<Folder> getFoldersByIds(List<String> ids) {
        return folderRepository.findAllById(ids);
    }


}
