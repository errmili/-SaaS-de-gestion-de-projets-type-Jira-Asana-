package com.projectsaas.file.dto;

import com.projectsaas.file.entity.FileShare;
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
public class FileShareDto {
    private UUID id;
    private UUID fileId;
    private String fileName;
    private UUID sharedBy;
    private UUID sharedWith;
    private FileShare.SharePermission permission;
    private String shareToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Integer accessCount;
    private LocalDateTime lastAccessedAt;
    private Boolean isPublic;
    private Boolean isExpired;
}