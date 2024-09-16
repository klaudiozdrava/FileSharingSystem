package com.kzdrava.webapp.utils;


import com.kzdrava.webapp.dto.DataCollection;
import org.unipi.client.SimpleHashGenerator;
import org.unipi.entities.CustomFile;
import org.unipi.entities.Folder;
import org.unipi.entities.Resource;
import org.unipi.entities.ResourceEnum;
import org.unipi.interfaces.HashGenerator;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kzdrava.webapp.constants.Constant.HASH_ALGORITHM;
import static com.kzdrava.webapp.constants.Constant.isFilePreviewAble;


public class DataCollectionUtils {

    private static final HashGenerator hashGenerator = new SimpleHashGenerator();


    public static String calculateHash(Resource resource) throws NoSuchAlgorithmException {
        String concatFields = resource.getName() + resource.getOwner() + resource.getResource().toString();
        if(Objects.nonNull(resource.getParentFolder())){
            concatFields += resource.getParentFolder().getId();
        }
        return  hashGenerator.calculateHash(concatFields, HASH_ALGORITHM);
    }

    public static List<String> getFilesIdsFromResources(List<Resource> resourceList) {
        return resourceList.stream()
                .filter(Objects::nonNull)  // Filters out null resources
                .filter(resource -> resource
                        .getResource().equals(ResourceEnum.FILE))  // Filters based on ResourceEnum.FILE
                .map(Resource::getId)  // Maps to resource IDs
                .collect(Collectors.toList());
    }

    public static List<String> getFoldersIdsFromResources(List<Resource> resources) {
       return resources.stream()
                .filter(Objects::nonNull)  // Filters out null resources
                .filter(resource -> resource
                        .getResource().equals(ResourceEnum.FOLDER))  // Filters based on ResourceEnum.FILE
                .map(Resource::getId)  // Maps to resource IDs
                .collect(Collectors.toList());
    }

    public static List<DataCollection> mapResourcesToDataCollections(List<Resource> resources) {
         return resources.stream()
                .map(resource -> {
                    if (resource.getResource().equals(ResourceEnum.FILE)) {
                        return mapFileToDataCollection((CustomFile) resource);
                    } else {
                        return mapFolderToDataCollection((Folder) resource);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<DataCollection> mapFilesToDataCollections(List<CustomFile> files) {
        return files.stream().map(
                DataCollectionUtils::mapFileToDataCollection
        ).toList();
    }

    public static DataCollection mapFileToDataCollection(CustomFile  file) {
        return DataCollection.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .preview(isFilePreviewAble(file.getName()))
                .modified(file.getLastModified())
                .owner(file.getOwner())
                .build();
    }

    public static List<DataCollection> mapFoldersToDataCollections(List<Folder> folders) {
        return folders.stream().map(
                DataCollectionUtils::mapFolderToDataCollection
        ).toList();
    }

    public static DataCollection mapFolderToDataCollection(Folder  folder) {
        return DataCollection.builder()
                .id(folder.getId())
                .name(folder.getName())
                .owner(folder.getOwner())
                .folder(true)
                .build();
    }

    public static List<DataCollection> concatDataCollections(List<DataCollection> folders, List<DataCollection> files) {
        return Stream.concat(folders.stream(), files.stream())
                .collect(Collectors.toList());
    }

}
