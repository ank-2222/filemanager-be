package com.file.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.file.manager.dto.filesystem.FileResponse;
import com.file.manager.dto.filesystem.UploadFileRequest;
import com.file.manager.models.File;
import com.file.manager.models.Metadata;
import com.file.manager.repository.FileRepository;
import com.file.manager.repository.FolderRepository;
import com.file.manager.repository.MetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {

    private final S3Client s3Client;
    private final SqsClient sqsClient;

    public FileService(S3Client s3Client, SqsClient sqsClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;

    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    public File uploadFile(UploadFileRequest request, UUID userId) throws JsonProcessingException {
        // Logic to upload file
        String folderPath = folderRepository.findFolderPathById(
                request.getFolderId() != null ? UUID.fromString(request.getFolderId()) : null
        );
        // Validate request, save file to storage, etc.
        String resolvedFileName = resolveDuplicateFileName(request.getFileName(), UUID.fromString(request.getFolderId()));
        String key = folderPath.substring(1) + "/" + resolvedFileName;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(request.getFileType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(request.getFile()));

        final String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;


        File file = File.builder()
                .id(UUID.randomUUID()) // Generate a new UUID for the file
                .name(resolvedFileName)
                .folderPath(folderPath)
                .mimeType(request.getFileType())
                .fileSize(request.getFileSize())
                .fileUrl(fileUrl)
                .s3Key(key)
                .ownerId(userId)
                .folderId(request.getFolderId() != null ? UUID.fromString(request.getFolderId()) : null) // Assuming folderId is provided
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        String messageBody = objectMapper.writeValueAsString(file);
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageGroupId("files")  // required for FIFO queues
                .messageDeduplicationId(UUID.randomUUID().toString())
                .build();

        SendMessageResponse response = sqsClient.sendMessage(sendMsgRequest);

        if (!response.messageId().isEmpty()) {
            fileRepository.save(file);
        }
        file.setOwnerId(UUID.randomUUID());
        return file;

    }

    public String resolveDuplicateFileName(String fileName, UUID folderId) {
        String baseName = FilenameUtils.getBaseName(fileName); // e.g. "mydoc"
        String extension = FilenameUtils.getExtension(fileName); // e.g. "pdf"
        String newName = fileName;
        int count = 1;
        while (fileRepository.existsByNameAndFolderId(newName, folderId)) {
            newName = baseName + " (" + count + ")" + (extension.isEmpty() ? "" : "." + extension);
            count++;
        }
        return newName;
    }


    @Transactional
    public File renameFile(UUID fileId, String newName, UUID userId) throws BadRequestException {
        if (newName == null || newName.isBlank()) {
            throw new BadRequestException("INVALID_FILE_NAME");
        }

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("FILE_NOT_FOUND"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BadRequestException("FORBIDDEN");
        }

        // Extract original parts
        String originalName = file.getName();
        String originalExt = FilenameUtils.getExtension(originalName);
        String requestedBase = FilenameUtils.getBaseName(newName).trim();

        if (requestedBase.isBlank()) {
            throw new BadRequestException("INVALID_FILE_NAME");
        }

        // Force extension to remain unchanged
        String finalName = requestedBase + (originalExt.isEmpty() ? "" : "." + originalExt);

        // Enforce uniqueness within the same folder
        if (!finalName.equals(originalName) &&
                fileRepository.existsByNameAndFolderId(finalName, file.getFolderId())) {
            finalName = resolveDuplicateFileNameWithLockedExt(requestedBase, originalExt, file.getFolderId());
        }

        file.setName(finalName);
        file.setUpdatedAt(LocalDateTime.now());
        return fileRepository.save(file);
    }

    private String resolveDuplicateFileNameWithLockedExt(String baseName, String ext, UUID folderId) {
        String candidate = baseName + (ext == null || ext.isEmpty() ? "" : "." + ext);
        int count = 1;
        while (fileRepository.existsByNameAndFolderId(candidate, folderId)) {
            candidate = baseName + " (" + count + ")" + (ext == null || ext.isEmpty() ? "" : "." + ext);
            count++;
        }
        return candidate;
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId) throws BadRequestException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("FILE_NOT_FOUND"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BadRequestException("NOT_FILE_OWNER");
        }


        // Delete S3 object first to avoid orphaned storage (idempotent if not found)
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build());

        // Delete DB record
        fileRepository.deleteById(fileId);
    }

    public FileResponse getFile(UUID fileId, UUID userId) throws BadRequestException {
        // Fetch the file
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("FILE_NOT_FOUND"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BadRequestException("NOT_FILE_OWNER");
        }

        // Fetch metadata if exists
        Optional<Metadata> metadataOpt = metadataRepository.findByFileId(fileId);

        // Build FileResponse
     return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .fileUrl(file.getFileUrl())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .folderId(file.getFolderId())
                .folderPath(file.getFolderPath())
                .s3Key(file.getS3Key())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .aiTags(metadataOpt.map(Metadata::getAiTag).orElse(null))
                .summary(metadataOpt.map(Metadata::getSummary).orElse(null))
                .sensitiveFlag(metadataOpt.map(Metadata::getSensitiveFlag).orElse(null))
                .confidentialFlag(metadataOpt.map(Metadata::getConfidentialFlag).orElse(null))
                .build();


    }
    @Transactional(readOnly = true)
    public List<FileResponse> getFilesInFolder(UUID folderId, UUID userId) throws BadRequestException {
        // Optional: Validate folder ownership if needed
        // folderRepository.findById(folderId) ...

        List<File> files = fileRepository.findByFolderIdAndOwnerId(folderId, userId);

        return files.stream().map(file -> {
            Optional<Metadata> metadataOpt = metadataRepository.findByFileId(file.getId());
            return FileResponse.builder()
                    .id(file.getId())
                    .name(file.getName())
                    .fileUrl(file.getFileUrl())
                    .mimeType(file.getMimeType())
                    .fileSize(file.getFileSize())
                    .folderId(file.getFolderId())
                    .folderPath(file.getFolderPath())
                    .s3Key(file.getS3Key())
                    .createdAt(file.getCreatedAt())
                    .updatedAt(file.getUpdatedAt())
                    .aiTags(metadataOpt.map(Metadata::getAiTag).orElse(null))
                    .summary(metadataOpt.map(Metadata::getSummary).orElse(null))
                    .sensitiveFlag(metadataOpt.map(Metadata::getSensitiveFlag).orElse(null))
                    .confidentialFlag(metadataOpt.map(Metadata::getConfidentialFlag).orElse(null))
                    .build();
        }).collect(Collectors.toList());
    }
}
