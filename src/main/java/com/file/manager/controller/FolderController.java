package com.file.manager.controller;


import com.file.manager.dto.filesystem.FolderRequest;
import com.file.manager.dto.filesystem.GetFolderResponse;
import com.file.manager.dto.filesystem.GetFolderRequest;
import com.file.manager.exception.FileSystemException;
import com.file.manager.models.Folder;
import com.file.manager.service.FolderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class FolderController {


    @Autowired
    private FolderService folderService;

    @PostMapping("/folder")
    public ResponseEntity<Folder> createFolder(@RequestBody  FolderRequest folderRequest,
                                               @RequestParam(name = "root", defaultValue = "false") boolean createRoot) throws BadRequestException {
        // Logic to create a folder
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());
            return ResponseEntity.ok(folderService.createFolder(folderRequest,createRoot,userId));
        } catch (Exception e) {
            if(e instanceof BadRequestException) {
                // Handle specific BadRequestException
                throw new BadRequestException(e.getMessage());
            }
            throw new FileSystemException(e.getMessage(), e);
        }

    }

    @GetMapping("/folder/{id}")
    public ResponseEntity<GetFolderResponse> getFolder(@PathVariable("id") String id) throws BadRequestException {
        // Logic to create a folder
        try {
            return ResponseEntity.ok(folderService.getFolder(id));
        } catch (Exception e) {
            if(e instanceof BadRequestException) {
                // Handle specific BadRequestException
                throw new BadRequestException(e.getMessage());
            }
            throw new FileSystemException(e.getMessage(), e);
        }

    }

    @PatchMapping("/folder/{id}/rename")
    public ResponseEntity<Folder> renameFolder(@PathVariable("id") String id,
                                               @RequestParam("name") String newName)
            throws BadRequestException {
        try {
            // Service method should validate name, enforce duplicate rules, and update timestamps
            Folder updated = folderService.renameFolder(UUID.fromString(id), newName);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new FileSystemException(e.getMessage(), e);
        }
    }

    @DeleteMapping("/folder/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable("id") String id)
            throws BadRequestException {
        try {
            folderService.deleteFolderRecursively(java.util.UUID.fromString(id)); // fallback to recursive if only one path exists
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("INVALID_FOLDER_ID");
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new FileSystemException(e.getMessage(), e);
        }
    }
    @GetMapping("/folder/root")
    public ResponseEntity<Folder> getUserRootFolder() throws BadRequestException {
        try {
            // Fetch userId from SecurityContext (token)
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            // Call service to fetch root folder + children
            Folder rootFolder = folderService.getUserRootFolder(userId);

            return ResponseEntity.ok(rootFolder);
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new FileSystemException(e.getMessage(), e);
        }
    }



}
