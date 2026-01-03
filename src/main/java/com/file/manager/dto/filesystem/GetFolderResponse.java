package com.file.manager.dto.filesystem;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetFolderResponse {


    private String folderName;
    private String folderPath;
    private List<ChildFolder> childFolder;
    private String creationDate;
    private String lastModifiedDate;

}
