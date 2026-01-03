package com.file.manager.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "folder", schema = "filesystem")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Folder {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private UUID userId;

    // Store only the ID of the parent folder
    @Column
    private UUID parentFolderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
