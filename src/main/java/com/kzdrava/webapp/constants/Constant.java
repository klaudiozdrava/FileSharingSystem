package com.kzdrava.webapp.constants;

import org.springframework.http.MediaType;
import java.util.Map;
import java.util.Objects;

public class Constant {


    public static int BATCH_SIZE_TO_BYTES = 3000;
    public static final String HASH_ALGORITHM = "SHA-256";

    public static final String ROOT = "root";


    private static final Map<String, MediaType> FILE_FORMAT = Map.of(
            ".pdf" ,MediaType.APPLICATION_PDF,
            ".txt" ,MediaType.TEXT_PLAIN,
            ".png", MediaType.IMAGE_PNG,
            ".jpg", MediaType.IMAGE_JPEG,
            ".gif", MediaType.IMAGE_GIF,
            ".xml",MediaType.APPLICATION_XML
    );

    public static MediaType getMediaType(String filename) {
        int lastIndexOf = filename.lastIndexOf('.');
        String extension = filename.substring(lastIndexOf);
        return FILE_FORMAT.get(extension);
    }

    public static boolean isFilePreviewAble(String filename) {
        return Objects.nonNull(getMediaType(filename));
    }
}
