package com.projectsaas.file.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "folder_id", columnDefinition = "UUID")
    private UUID folderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", columnDefinition = "UUID")
    private UUID entityId;

    @Column(name = "uploaded_by", nullable = false, columnDefinition = "UUID")
    private UUID uploadedBy;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FileStatus status;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status", length = 20)
    private VirusScanStatus virusScanStatus;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        uploadedAt = now;
        updatedAt = now;
        if (isPublic == null) {
            isPublic = false;
        }
        if (status == null) {
            status = FileStatus.ACTIVE;
        }
        if (virusScanStatus == null) {
            virusScanStatus = VirusScanStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum FileStatus {
        ACTIVE, DELETED, ARCHIVED
    }

    public enum EntityType {
        PROJECT, TASK, USER, ORGANIZATION, OTHER
    }

    public enum VirusScanStatus {
        PENDING, CLEAN, INFECTED, ERROR
    }

    // Méthodes de compatibilité pour l'ancien code
    public Long getSize() {
        return this.fileSize;
    }

    public void setSize(Long size) {
        this.fileSize = size;
    }

    public String getPath() {
        return this.filePath;
    }

    public void setPath(String path) {
        this.filePath = path;
    }

    public String getUserId() {
        return this.uploadedBy != null ? this.uploadedBy.toString() : null;
    }
}