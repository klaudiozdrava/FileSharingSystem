package com.kzdrava.webapp.dto;

import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataCollection{
    private String id;
    private String owner;
    private Date modified;
    private String name;
    private long size;
    private boolean preview;
    private boolean folder;
}
