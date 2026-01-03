package com.file.manager.dto.filesystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequest {

    private String folderId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private byte[] file;
}
