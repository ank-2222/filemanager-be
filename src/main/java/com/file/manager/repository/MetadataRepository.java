package com.file.manager.repository;

import com.file.manager.models.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    // Additional custom methods if needed
    Optional<com.file.manager.models.Metadata> findByFileId(UUID fileId);
}
