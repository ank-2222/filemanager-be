package com.file.manager.dto.filesystem;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderRequest {


    private String name;
    private String parentFolderId;



}
