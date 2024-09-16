package com.kzdrava.webapp.constants;

import org.springframework.http.MediaType;
import java.util.Map;
import java.util.Objects;

public class Constant {
    public static final String AUTHENTICATION_URL = "http://localhost:8082/api/auth";
    public static final String REGISTER =  "/register";
    public static final String LOGIN = "/authenticate";

    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String USER_SAVED = "User registered successfully!";

    public static int BATCH_SIZE_TO_BYTES = 3000;
    public static final String HASH_ALGORITHM = "SHA-256";

    public static final String ROOT = "root";
    public static final String NO_PARENT = "none";
//    public static final String OWNER = "kzdrava@unipi.gr";

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
