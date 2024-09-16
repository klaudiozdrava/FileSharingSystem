package com.kzdrava.webapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    private String name;
    private byte[] fileContent;
    private long size;

}
