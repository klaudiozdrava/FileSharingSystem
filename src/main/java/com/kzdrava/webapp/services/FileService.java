package com.kzdrava.webapp.services;

import com.kzdrava.webapp.configs.Config;
import com.kzdrava.webapp.dto.FileDto;
import com.kzdrava.webapp.utils.DataCollectionUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.unipi.client.SimpleChunksHandler;
import org.unipi.client.SimpleFileChunkSplitter;
import org.unipi.client.SimpleHashGenerator;
import org.unipi.entities.Chunk;
import org.unipi.entities.CustomFile;
import org.unipi.entities.Folder;
import org.unipi.interfaces.ChunksHandler;
import org.unipi.interfaces.FileChunkSplitter;
import org.unipi.interfaces.HashGenerator;
import org.unipi.repositories.ChunkRepository;
import org.unipi.repositories.FileRepository;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kzdrava.webapp.constants.Constant.*;
import static org.unipi.entities.ResourceEnum.FILE;

@Service
@Slf4j(topic = "FileService")
public class FileService {

    private final FileRepository fileRepository;
    private final FileChunkSplitter fileChunkSplitter;
    private final SharingCollectionsService sharingCollectionsService;
    private final HashGenerator hashGenerator;
    private final ChunkRepository chunkRepository;
    private final ChunksHandler chunksHandler;

    public FileService(FileRepository fileRepository,
                       ChunkRepository chunkRepository,
                       SharingCollectionsService sharingCollectionsService) {

        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.sharingCollectionsService = sharingCollectionsService;
        this.fileChunkSplitter = new SimpleFileChunkSplitter();
        this.hashGenerator = new SimpleHashGenerator();
        this.chunksHandler = new SimpleChunksHandler();
    }

    @Transactional
    public void updateFile(FileDto fileDto, Folder folder) throws NoSuchAlgorithmException {

        CustomFile file = createFileObject(fileDto, folder);
        List<Chunk> chunks = splitFileToChunks(fileDto.getFileContent());
        file.setChunks(chunks);

        if(fileDoesNotExist(file)) {
            chunks.forEach(chunk -> chunk.setFile(file));
            fileRepository.save(file);
        }
        updateOnlyNewChunks(chunks, file.getId());

    }

    private CustomFile createFileObject(FileDto fileDto, Folder folder) throws NoSuchAlgorithmException {
        CustomFile file = new CustomFile(fileDto.getName(), folder.getOwner(), FILE);
        file.setSize(fileDto.getSize());
        file.setLastModified(new Date());
        file.setParentFolder(folder);
        file.setId(DataCollectionUtils.calculateHash(file));
        return file;
    }


    private boolean fileDoesNotExist(CustomFile file) {
        return !fileRepository.existsById(file.getId());
    }


    private List<Chunk> splitFileToChunks(byte[] fileBytes)  {

        List<byte[]> listOfBytes = fileChunkSplitter
                .splitFileToChunks(fileBytes, BATCH_SIZE_TO_BYTES);

        List<Chunk> chunks = new ArrayList<>();

        for(int i = 0; i< listOfBytes.size(); i++) {
            byte[] byteArray = listOfBytes.get(i);
            log.info("Split size {}", byteArray.length);
            Chunk chunk ;
            try {
                chunk = Chunk.builder()
                        .chunkData(byteArray)
                        .hashValue(hashGenerator
                                .calculateHash(byteArray, HASH_ALGORITHM ))
                        .position(i)
                        .build();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            chunks.add(chunk);
        }
        return chunks;

    }


    @Transactional
    private void updateOnlyNewChunks (List<Chunk> newChunks, String fileId) {

        List<Chunk> existingChunks = chunkRepository.findByFile_Id(fileId);

        int minSize = Math.min(existingChunks.size(), newChunks.size());
        int i;

        // Update existing chunks
        for (i = 0; i < minSize; i++) {
            Chunk existingChunk = existingChunks.get(i);
            Chunk newChunk = newChunks.get(i);
            if (newChunk.getPosition() == existingChunk.getPosition()
                    && !newChunk.getHashValue().equals(existingChunk.getHashValue())) {
                existingChunk.setHashValue(newChunk.getHashValue());
                existingChunk.setChunkData(newChunk.getChunkData());
            }
        }

        // Add remaining new chunks if newChunks is larger
        if (newChunks.size() > existingChunks.size()) {
            existingChunks.addAll(newChunks.subList(i, newChunks.size()));
        }

        // Save updated chunks
        chunkRepository.saveAll(existingChunks);
    }

    public byte[] constructFileContentFromChunks(CustomFile file) {
        List<Chunk> chunkList = file.getChunks();
        log.info("All files {}", chunkList);
        return chunksHandler.constructFileFromChunks(chunkList);
    }


    public CustomFile getFileFromDBById(String id) {
        return fileRepository.findById(id).orElse(null);
    }

    public List<CustomFile> getFilesByIds(List<String> ids) {
        return fileRepository.findAllById(ids);
    }



}
