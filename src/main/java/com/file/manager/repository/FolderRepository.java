package com.file.manager.repository;

import com.file.manager.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    // Additional custom methods if needed
    boolean existsByNameAndParentFolderId(String name, UUID parentFolderId);

    @Query("SELECT f.id, f.name, f.updatedAt FROM Folder f WHERE f.parentFolderId = :parentFolderId")
    List<Object[]> findChildFolderByParentFolderId(@Param("parentFolderId") UUID parentFolderId);
    long countByParentFolderId(UUID parentFolderId);

    List<Folder> findAllByParentFolderId(UUID parentFolderId);

    @Query("SELECT f FROM Folder f WHERE f.userId = :userId AND f.parentFolderId IS NULL")
    Optional<Folder> findRootFolderByUserId(@Param("userId") UUID userId);


    @Query("SELECT f.path FROM Folder f WHERE f.id = :id")
    String findFolderPathById(@Param("id") UUID id);

    @Modifying
    @Query(value = """
UPDATE filesystem.folder
SET path = CONCAT(:newPrefix, SUBSTRING(path FROM LENGTH(:oldPrefix) + 1))
WHERE path LIKE CONCAT(:oldPrefix, '%')
""", nativeQuery = true)
    void bulkUpdateDescendantPaths(@Param("oldPrefix") String oldPrefix,
                                  @Param("newPrefix") String newPrefix);

}
