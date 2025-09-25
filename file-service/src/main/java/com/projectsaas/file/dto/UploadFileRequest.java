package com.projectsaas.file.dto;

import com.projectsaas.file.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequest {
    private UUID folderId;
    private String entityType; // String au lieu d'enum pour flexibilité
    private UUID entityId;
    private Boolean isPublic;
    private Boolean allowDuplicates;
    private String description;
    private String tags;

    // Méthode helper pour convertir String vers EntityType
    public FileEntity.EntityType getEntityTypeEnum() {
        if (entityType == null || entityType.trim().isEmpty()) {
            return FileEntity.EntityType.OTHER;  // ← CHANGÉ : NONE → OTHER
        }

        try {
            return FileEntity.EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FileEntity.EntityType.OTHER;  // ← CHANGÉ : NONE → OTHER
        }
    }
}