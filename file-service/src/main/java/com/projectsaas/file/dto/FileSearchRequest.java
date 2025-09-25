package com.projectsaas.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequest {
    private String search;
    private String contentType;
    private UUID folderId;
    private String entityType;
    private UUID entityId;
    private Long minSize;
    private Long maxSize;
    private String extension;
}
