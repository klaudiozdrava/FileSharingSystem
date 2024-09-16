package com.kzdrava.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentFolder {
    private String id;
    private List<DataCollection> dataCollectionList;
}
