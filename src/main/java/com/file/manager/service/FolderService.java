package com.file.manager.service;

import com.file.manager.dto.filesystem.ChildFolder;
import com.file.manager.dto.filesystem.FolderRequest;
import com.file.manager.dto.filesystem.GetFolderResponse;
import com.file.manager.exception.FileSystemException;
import com.file.manager.models.Folder;
import com.file.manager.repository.FolderRepository;
import com.file.manager.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private PathUtils pathUtils;

    public Folder createFolder(FolderRequest folder, Boolean createRoot, UUID userId) throws BadRequestException {
        final UUID parentId = folder.getParentFolderId() != null ? UUID.fromString(folder.getParentFolderId()) : null;

        // Resolve parent path
        final String parentFolderPath;
        if (Boolean.TRUE.equals(createRoot)) {
            // Root has no parent; its path is "/"
            parentFolderPath = null;
        } else {
            if (parentId == null) {
                throw new BadRequestException("PARENT_FOLDER_REQUIRED");
            }
            parentFolderPath = folderRepository.findFolderPathById(parentId);
            if (parentFolderPath == null) {
                throw new BadRequestException("PARENT_FOLDER_NOT_FOUND");
            }
        }

        // Determine display name and sanitized path segment
        final String rawName = Boolean.TRUE.equals(createRoot) ? userId.toString() : folder.getName();
        if (rawName == null || rawName.isBlank()) {
            throw new BadRequestException("INVALID_FOLDER_NAME");
        }

        final String segment = PathUtils.toSegment(rawName);
        if (segment.isBlank()) {
            throw new BadRequestException("INVALID_FOLDER_NAME");
        }

        // Compute final path
        final String computedPath = Boolean.TRUE.equals(createRoot)
                ?  "/"+userId
                : PathUtils.join(parentFolderPath, rawName); // join will sanitize based on rawName

        // Duplicate per parent (display name used for UX-level uniqueness)
        if (folderRepository.existsByNameAndParentFolderId(rawName, Boolean.TRUE.equals(createRoot) ? null : parentId)) {
            throw new BadRequestException("DUPLICATE_FOLDER_NAME");
        }

        Folder newFolder = Folder.builder()
                .id(UUID.randomUUID())
                .name(rawName)                             // display name
                .path(computedPath)
                .userId(userId)// normalized path
                .parentFolderId(Boolean.TRUE.equals(createRoot) ? null : parentId)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        folderRepository.save(newFolder);
        return newFolder;
    }


    public GetFolderResponse getFolder(String folderId) throws BadRequestException {
        log.info("Fetching folder with ID: {}", folderId);
        Folder folder = folderRepository.findById(UUID.fromString(folderId))
                .orElseThrow(() -> new BadRequestException("FOLDER_NOT_FOUND"));

        log.info("Folder found: {}", folder);
        List<Object[]> childFolderData = folderRepository.findChildFolderByParentFolderId(folder.getId());
        List<ChildFolder> childFolders = childFolderData.stream()
                .map(obj -> ChildFolder.builder()
                        .folderId((UUID) obj[0])
                        .folderName((String) obj[1])
                        .updatedAt(obj[2].toString())
                        .build())
                .toList();

        return GetFolderResponse.builder()
                .folderName(folder.getName())
                .folderPath(folder.getPath())
                .childFolder(childFolders)
                .creationDate(folder.getCreatedAt().toString())
                .lastModifiedDate(folder.getUpdatedAt().toString())
                .build();
    }

    // ========== NEW: UPDATE FOLDER NAME ==========
    @Transactional
    public Folder renameFolder(UUID folderId, String newName) throws BadRequestException {
        if (newName == null || newName.isBlank()) {
            throw new BadRequestException("INVALID_FOLDER_NAME");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("FOLDER_NOT_FOUND"));

        UUID parentId = folder.getParentFolderId();

        // Prevent duplicate names under the same parent
        boolean duplicate = folderRepository.existsByNameAndParentFolderId(newName, parentId);
        if (duplicate && !newName.equals(folder.getName())) {
            throw new BadRequestException("DUPLICATE_FOLDER_NAME");
        }

        // Derive parentPath
        final String parentPath;
        if (parentId == null) {
            parentPath = "/"; // root’s parent is conceptual root
        } else {
            String p = folderRepository.findFolderPathById(parentId);
            if (p == null) throw new BadRequestException("PARENT_FOLDER_NOT_FOUND");
            parentPath = p;
        }

        // Build sanitized new segment and new full path
        String oldPath = folder.getPath();                // e.g. "/docs/reports/q1"
        String oldPrefix = oldPath.endsWith("/") ? oldPath : oldPath + "/";

        String newSegment = PathUtils.toSegment(newName); // same sanitizer you use when creating paths
        if (newSegment.isBlank()) {
            throw new BadRequestException("INVALID_FOLDER_NAME");
        }

        String normalizedParent = normalizeParent(parentPath); // ensure leading "/" and no trailing "/"
        String newPath = normalizedParent.equals("/") ? "/" + newSegment : normalizedParent + "/" + newSegment;
        String newPrefix = newPath + "/";

        // Update current folder
        folder.setName(newName);
        folder.setPath(newPath);
        folder.setUpdatedAt(java.time.LocalDateTime.now());
        folderRepository.save(folder);

        // Bulk update all descendants’ paths by prefix replace
        // Prefer a single SQL update for performance
        folderRepository.bulkUpdateDescendantPaths(oldPrefix, newPrefix);

        return folder;
    }

    private String normalizeParent(String parentPath) {
        if (parentPath == null || parentPath.isBlank()) return "/";
        String p = parentPath.trim();
        if (!p.startsWith("/")) p = "/" + p;
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    // ========== NEW: DELETE FOLDER ==========
    @Transactional
    public void deleteFolderRecursively(UUID folderId) throws BadRequestException {

        Folder root = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("FOLDER_NOT_FOUND"));

        // Load full subtree and delete bottom-up
        List<UUID> toDelete = new ArrayList<>();
        collectSubtreeIds(folderId, toDelete); // includes children and descendants
        toDelete.add(folderId);


        for (UUID fid : toDelete) {
            folderRepository.deleteById(fid);
        }
    }


    // Utility: BFS/DFS to collect subtree IDs
    private void collectSubtreeIds(UUID parentId, List<UUID> accumulator) {
        List<Folder> children = folderRepository.findAllByParentFolderId(parentId);
        for (Folder child : children) {
            accumulator.add(child.getId());
            collectSubtreeIds(child.getId(), accumulator);
        }
    }
    public Folder getUserRootFolder(UUID userId) {
        // Example logic:
        // - Find the folder where parentId is null and userId = userId
        // - Fetch children (could be lazy/eager depending on JPA setup)
        return folderRepository.findRootFolderByUserId(userId)
                .orElseThrow(() -> new FileSystemException("ROOT_FOLDER_NOT_FOUND"));
    }


}
