package com.projectsaas.file.service;

import com.projectsaas.file.dto.*;
import com.projectsaas.file.entity.FileEntity;
import com.projectsaas.file.entity.FileShare;
import com.projectsaas.file.exception.FileNotFoundException;
import com.projectsaas.file.exception.UnauthorizedException;
import com.projectsaas.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.springframework.web.multipart.MultipartFile;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final AuthIntegrationService authIntegrationService;

    public FileDto uploadFile(MultipartFile file, UploadFileRequest request, String token) {
        log.info("Uploading file: {}", file.getOriginalFilename());

        // Validation basique du token
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        UUID tenantId = extractTenantIdFromToken(token);
        UUID uploadedBy = extractUserIdFromToken(token);

        // Validation du fichier
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > 100 * 1024 * 1024) { // 100MB
            throw new IllegalArgumentException("File size exceeds maximum allowed (100MB)");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // Check for duplicates if not allowed
        if (!request.getAllowDuplicates()) {
            boolean exists = fileRepository.existsByOriginalNameAndTenantIdAndFolderIdAndStatus(
                    file.getOriginalFilename(), tenantId, request.getFolderId(), FileEntity.FileStatus.ACTIVE);
            if (exists) {
                throw new IllegalStateException("File with this name already exists in the folder");
            }
        }

        // Store file locally pour tests
        String storedName = generateStoredName(file.getOriginalFilename());
        String storedPath = saveFileLocally(file, storedName, tenantId);

        FileEntity fileEntity = FileEntity.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(storedPath)
                .folderId(request.getFolderId())
                .entityType(parseEntityType(request.getEntityType()))
                .entityId(request.getEntityId())
                .uploadedBy(uploadedBy)
                .isPublic(request.getIsPublic())
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(FileEntity.FileStatus.ACTIVE)
                .virusScanStatus(FileEntity.VirusScanStatus.CLEAN) // Pour tests
                .build();

        FileEntity savedFile = fileRepository.save(fileEntity);
        log.info("File uploaded successfully with ID: {}", savedFile.getId());

        return mapToDto(savedFile);
    }

    // Méthode locale pour sauvegarder les fichiers (pour tests)
    private String saveFileLocally(MultipartFile file, String storedName, UUID tenantId) {
        try {
            // ✅ CHEMIN ABSOLU - vers votre dossier projet
            String projectDir = System.getProperty("user.dir");
            String baseDir = projectDir + File.separator + "temp-storage" + File.separator + tenantId.toString();

            File directory = new File(baseDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new RuntimeException("Could not create directory: " + baseDir);
                }
                log.info("Created directory: {}", baseDir);
            }

            // Sauvegarder le fichier
            String filePath = baseDir + File.separator + storedName;
            File destFile = new File(filePath);
            file.transferTo(destFile);

            log.info("File saved successfully to: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error saving file locally: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public FileDto getFileById(UUID fileId, String token) {
        log.info("Getting file by ID: {}", fileId);

        UUID tenantId = extractTenantIdFromToken(token);

//        FileEntity file = fileRepository.findByIdAndTenantId(fileId, tenantId)
//                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // TEMPORAIRE : Chercher sans restriction de tenant pour debug
        Optional<FileEntity> fileOpt = fileRepository.findById(fileId);

        if (fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            log.info("File found! File tenantId: {}, Request tenantId: {}", file.getTenantId(), tenantId);
            return mapToDto(file);
        } else {
            log.error("File not found in database: {}", fileId);
            throw new FileNotFoundException("File not found in database: " + fileId);
        }

        //return mapToDto(file);
    }

//    public FileDownloadResponse downloadFile(UUID fileId, String token) throws IOException {
//        log.info("Downloading file: {}", fileId);
//
//        UUID tenantId = extractTenantIdFromToken(token);
//
//        FileEntity file = fileRepository.findByIdAndTenantId(fileId, tenantId)
//                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
//
//        InputStream inputStream = getFileInputStream(file.getFilePath());
//
//        return FileDownloadResponse.builder()
//                .filename(file.getOriginalName())
//                .contentType(file.getContentType())
//                .fileSize(file.getFileSize())
//                .inputStream(inputStream)
//                .build();
//    }

    public FileDownloadResponse downloadFile(UUID fileId, String token) throws IOException {
        log.info("Downloading file: {}", fileId);

        // TEMPORAIRE : Chercher sans filtre tenant pour tests
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        InputStream inputStream = getFileInputStream(file.getFilePath());

        return FileDownloadResponse.builder()
                .filename(file.getOriginalName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .inputStream(inputStream)
                .build();
    }
    @Transactional(readOnly = true)
    public Page<FileDto> getFiles(FileSearchRequest searchRequest, Pageable pageable, String token) {
        log.info("Getting files with search: {}", searchRequest.getSearch());

        UUID tenantId = extractTenantIdFromToken(token);
        FileEntity.FileStatus status = FileEntity.FileStatus.ACTIVE;

        Page<FileEntity> files;

        if (searchRequest.getSearch() != null && !searchRequest.getSearch().trim().isEmpty()) {
            files = fileRepository.searchFiles(searchRequest.getSearch(), tenantId, status, pageable);
        } else if (searchRequest.getFolderId() != null) {
            files = fileRepository.findByTenantIdAndFolderIdAndStatus(tenantId, searchRequest.getFolderId(), status, pageable);
        } else {
            files = fileRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        }

        return files.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<FileDto> getFolderFiles(UUID folderId, String token) {
        log.info("Getting files for folder: {}", folderId);

        UUID tenantId = extractTenantIdFromToken(token);
        FileEntity.FileStatus status = FileEntity.FileStatus.ACTIVE;

        List<FileEntity> files;
        if (folderId == null) {
            files = fileRepository.findByFolderIdIsNullAndTenantIdAndStatus(tenantId, status);
        } else {
            files = fileRepository.findByFolderIdAndTenantIdAndStatus(folderId, tenantId, status);
        }

        return files.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteFile(UUID fileId, String token) {
        log.info("Deleting file: {}", fileId);

        UUID tenantId = extractTenantIdFromToken(token);

        FileEntity file = fileRepository.findByIdAndTenantId(fileId, tenantId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Soft delete
        file.setStatus(FileEntity.FileStatus.DELETED);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(file);

        log.info("File deleted successfully: {}", fileId);
    }

    @Transactional(readOnly = true)
    public List<FileDto> searchFiles(String query, String token) {
        log.info("Searching files with query: {}", query);

        UUID tenantId = extractTenantIdFromToken(token);
        FileEntity.FileStatus status = FileEntity.FileStatus.ACTIVE;

        List<FileEntity> files = fileRepository.searchFilesByName(query, tenantId, status);

        return files.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public FileShareDto shareFile(UUID fileId, ShareFileRequest request, String token) {
        log.info("Sharing file: {}", fileId);

        UUID tenantId = extractTenantIdFromToken(token);
        UUID sharedBy = extractUserIdFromToken(token);

        FileEntity file = fileRepository.findByIdAndTenantId(fileId, tenantId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        FileShareDto shareDto = FileShareDto.builder()
                .id(UUID.randomUUID())
                .fileId(fileId)
                .fileName(file.getOriginalName())
                .sharedBy(sharedBy)
                .shareToken(UUID.randomUUID().toString())
                .permission(FileShare.SharePermission.valueOf(request.getPermission().toString()))
                .createdAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .accessCount(0)
                .isPublic(request.getSharedWith() == null)
                .isExpired(false)
                .build();

        log.info("File shared successfully: {}", fileId);
        return shareDto;
    }

    private FileDto mapToDto(FileEntity file) {
        String extension = "";
        if (file.getOriginalName() != null && file.getOriginalName().contains(".")) {
            extension = file.getOriginalName().substring(file.getOriginalName().lastIndexOf("."));
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        Boolean isImage = contentType.startsWith("image/");
        Boolean isVideo = contentType.startsWith("video/");
        Boolean isPdf = contentType.equals("application/pdf");

        return FileDto.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .humanReadableSize(formatFileSize(file.getFileSize()))
                .status(file.getStatus())
                .uploadedBy(file.getUploadedBy())
                .uploadedAt(file.getUploadedAt())
                .updatedAt(file.getUpdatedAt())
                .entityType(file.getEntityType() != null ? file.getEntityType().toString() : null)
                .entityId(file.getEntityId())
                .isPublic(file.getIsPublic())
                .virusScanStatus(file.getVirusScanStatus())
                .extension(extension)
                .isImage(isImage)
                .isVideo(isVideo)
                .isPdf(isPdf)
                .folderId(file.getFolderId())
                .path(file.getFilePath())
                .width(null)
                .height(null)
                .duration(null)
                .checksum(file.getChecksum())
                .thumbnailSmall(null)
                .thumbnailMedium(null)
                .thumbnailLarge(null)
                .build();
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    private UUID extractTenantIdFromToken(String token) {
        return authIntegrationService.extractTenantId(token);
    }

    private UUID extractUserIdFromToken(String token) {
        return authIntegrationService.extractUserId(token);
    }

    private String generateStoredName(String originalName) {
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    // Méthode pour lire les fichiers depuis le disque local
    private InputStream getFileInputStream(String path) throws IOException {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new FileInputStream(file);
            } else {
                log.warn("File not found: {}", path);
                return new ByteArrayInputStream("File not found".getBytes());
            }
        } catch (Exception e) {
            log.error("Error reading file: " + path, e);
            return new ByteArrayInputStream("Error reading file".getBytes());
        }
    }

    private FileEntity.EntityType parseEntityType(String entityType) {
        if (entityType == null) return null;
        try {
            return FileEntity.EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FileEntity.EntityType.OTHER;
        }
    }
}