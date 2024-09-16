package com.kzdrava.webapp.utils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static com.kzdrava.webapp.constants.Constant.*;


public class ResourceUtils {


    public static ResponseEntity<Resource> handleBrowserFiles(byte[] chunkBlobs, ContentDisposition contentDisposition)  {

        ByteArrayResource resource = new ByteArrayResource(chunkBlobs);


        HttpHeaders headers = new HttpHeaders();
        assert contentDisposition.getFilename() != null;
        headers.setContentType(getMediaType(contentDisposition.getFilename()));
        headers.setContentDisposition(contentDisposition);
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(resource, headers, org.apache.http.HttpStatus.SC_OK);
    }






}
