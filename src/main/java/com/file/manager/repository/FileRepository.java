package com.file.manager.repository;

import com.file.manager.models.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    // Additional custom methods if needed]

    Boolean existsByNameAndFolderId(String name, UUID folderId);


    List<File> findByFolderIdAndOwnerId(UUID folderId, UUID ownerId);
}