package com.projectsaas.file.dto;

import com.projectsaas.file.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private UUID id;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long fileSize;
    private String humanReadableSize;
    private FileEntity.FileStatus status;
    private UUID uploadedBy;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private String entityType; // String au lieu d'enum pour API REST
    private UUID entityId;
    private Boolean isPublic;
    private FileEntity.VirusScanStatus virusScanStatus;
    private String extension;
    private Boolean isImage;
    private Boolean isVideo;
    private Boolean isPdf;
    private UUID folderId;
    private String path;
    private Integer width;
    private Integer height;
    private Integer duration;
    private String checksum;
    private String thumbnailSmall;
    private String thumbnailMedium;
    private String thumbnailLarge;
}
