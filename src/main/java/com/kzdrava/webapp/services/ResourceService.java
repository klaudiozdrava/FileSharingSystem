package com.kzdrava.webapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unipi.entities.Resource;
import org.unipi.repositories.ResourceRepository;
import org.unipi.repositories.ShareRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ShareRepository shareRepository;
    public Resource getResourceById(String id) {
        return resourceRepository.findById(id).orElse(null);
    }

    public List<Resource> getResourcesByIds(List<String> ids) {
        return resourceRepository.findAllById(ids);
    }

    @Transactional
    public void deleteResourceById(String id, String username) {
        Optional<Resource> resource = resourceRepository.findById(id);
        if(resource.isPresent() && resource.get().getOwner().equals(username))
            resourceRepository.deleteById(id);
        else resource.ifPresent(value -> shareRepository.deleteByResourceReceiverAndResource(username, value));
    }

}
