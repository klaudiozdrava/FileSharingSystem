package com.kzdrava.webapp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unipi.entities.*;
import org.unipi.repositories.ResourceRepository;
import org.unipi.repositories.ShareRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "SharingCollectionsService")
@RequiredArgsConstructor
public class SharingCollectionsService {

    private final ShareRepository shareRepository;
    private final ResourceRepository resourceRepository;
    private static final String SHARED_TO_WHO_HAS_LINK = "All";

    public void rejectSharingRequestForResource(String id, String receiver) {
        shareRepository.deleteByResourceIdAndResourceReceiver(id, receiver);
    }

    public void updateSharingResourceStatus(String id) {
       // shareRepository.updateStatusByResourceId(id, Status.ACCEPTED);
    }

    public ResponseEntity<String> shareResource(Resource resource, Set<String> emails, String sharer) {

        shareRepository.deleteByResourceReceiverAndResource(SHARED_TO_WHO_HAS_LINK, resource);

        List<Share> shares = new ArrayList<>();

        Status status = resource.getOwner().equals(sharer) ? Status.ACCEPTED : Status.PENDING;

        for (String receiver : emails) {
                Share share = new Share();
                share.setResource(resource);
                share.setResourceReceiver(receiver);
                share.setResourceSharer(sharer);
                share.setStatus(status);

                shares.add(share);
        }
        shareRepository.saveAll(shares);

        return ResponseEntity.ok().body("Resource named: " + resource.getName() + " shared successfully");

    }

    //TODO check if resource exists in shaes for particular receiver
    @Transactional
    public ResponseEntity<String> shareResourceLink(Resource resource, String sharer) {

        shareRepository.deleteAllByResource(resource);

        Status status = resource.getOwner().equals(sharer) ? Status.ACCEPTED : Status.PENDING;

        Share share = Share.builder()
                    .resource(resource)
                    .resourceReceiver(SHARED_TO_WHO_HAS_LINK)
                    .resourceSharer(sharer)
                    .status(status)
                    .build();

        shareRepository.save(share);
        return ResponseEntity.ok().body("Resource named: " + resource.getName() + " shared successfully");

    }

    public ResponseEntity<String> findSharedResourcesForUser(String id, String user) {
        List<Share> shares = shareRepository.findByResourceId(id);
        if(!shares.isEmpty() && (isUserAllowedToAccessResource(user, shares) || isUserOwnerOfResource(user,id))) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("User is not allowed to access the resource");
    }

    private boolean isUserAllowedToAccessResource(String user, List<Share> shares) {
        return shares.stream().anyMatch(share -> share
                .getResourceReceiver()
                .equals(user) || share
                .getResourceReceiver()
                .equals(SHARED_TO_WHO_HAS_LINK));
    }

    private boolean isUserOwnerOfResource(String user, String id){
       Optional<Resource> resource = resourceRepository.findById(id);
       return resource.isPresent() && resource.get().getOwner().equals(user);
    }

    public List<Resource> retrieveSharedResourcesByUser(String user) {
        return shareRepository
                .findByResourceSharer(user)
                .stream()
                .filter(Objects::nonNull) // Ensure the Share object is not null
                .map(Share::getResource)
                .collect(Collectors.toList());
    }

    public List<Resource> retrieveSharedReceivedResources(String user) {
        return shareRepository.findByResourceReceiverAndStatus(user, Status.ACCEPTED)
                .stream()
                .filter(Objects::nonNull)
                .map(Share::getResource)
                .collect(Collectors.toList());
    }

    public List<String> findUsersHavingAccessToResource(String id) {
        List<Share> shares = shareRepository.findByResourceIdAndStatus(id, Status.ACCEPTED);
        return shares.stream().map(Share::getResourceReceiver).toList();
    }

    public void revokeResourceSharingToUser(String id, String username) {
        shareRepository.deleteByResourceIdAndResourceReceiver(id, username);
    }

    public List<Share> findOwnerPendingShares(String username) {
        return shareRepository.findByResourceOwnerAndStatus(username, Status.PENDING);
    }

}
