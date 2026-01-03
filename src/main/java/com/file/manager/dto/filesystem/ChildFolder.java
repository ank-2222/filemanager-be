package com.file.manager.dto.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChildFolder {
    private UUID folderId;
    private String folderName;
    private String updatedAt;

}
