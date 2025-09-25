package com.projectsaas.file.dto;

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
public class FolderDto {
    private UUID id;
    private String name;
    private String path;
    private UUID parentId;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer depth;
    private Boolean isRoot;
    private Integer subfolderCount;
    private Integer fileCount;
}