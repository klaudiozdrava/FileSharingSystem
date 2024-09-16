package com.kzdrava.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingShare {
    private String id;
    private String resourceName;
    private String receiver;
}
