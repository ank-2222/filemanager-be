package com.file.manager.dto.filesystem;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private UUID id;
    private String name;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private UUID folderId;
    private String folderPath;
    private String s3Key;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Metadata fields
    private List<String> aiTags;
    private String summary;
    private Boolean sensitiveFlag;
    private Boolean confidentialFlag;
}
