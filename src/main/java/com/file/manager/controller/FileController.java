package com.file.manager.controller;

import com.file.manager.dto.filesystem.FileResponse;
import com.file.manager.dto.filesystem.UploadFileRequest;
import com.file.manager.models.File;
import com.file.manager.service.FileService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<File> uploadFile(@RequestBody @Validated UploadFileRequest uploadFileRequest)
            throws BadRequestException {
        try {
            // Allowed MIME types
            List<String> allowedTypes = Arrays.asList(
                    "application/pdf",
                    "image/jpeg",
                    "image/png",
                    "text/plain"
            );

            if (!allowedTypes.contains(uploadFileRequest.getFileType())) {
                throw new BadRequestException("INVALID_FILE_TYPE");
            }

            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            return ResponseEntity.ok(fileService.uploadFile(uploadFileRequest, userId));
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new RuntimeException(e.getMessage());
        }
    }


    // GET: fetch file metadata by ID (owned by current user)
    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable("id") String id) throws BadRequestException {
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            FileResponse file = fileService.getFile(UUID.fromString(id), userId);
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    // DELETE: delete file by ID (S3 + DB), only if owned by current user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable("id") String id) throws BadRequestException {
        try {

            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            fileService.deleteFile(UUID.fromString(id), userId); // or deleteFile(fileId, userId) if typed
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    // Optional: PATCH rename
    @PatchMapping("/{id}/rename")
    public ResponseEntity<File> renameFile(@PathVariable("id") String id,
                                           @RequestParam("name") String newName) throws BadRequestException {
        try {

            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            File updated = fileService.renameFile(UUID.fromString(id), newName, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new RuntimeException(e.getMessage());
        }
    }
    // GET: fetch all files in a specific folder for the current user
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<FileResponse>> getFilesInFolder(@PathVariable("folderId") String folderId) throws BadRequestException {
        try {
            // Get current user ID from SecurityContext
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(userDetails.getUsername());

            // Call service to fetch files in folder
            List<FileResponse> files = fileService.getFilesInFolder(UUID.fromString(folderId), userId);

            return ResponseEntity.ok(files);
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new RuntimeException(e.getMessage());
        }
    }


}
