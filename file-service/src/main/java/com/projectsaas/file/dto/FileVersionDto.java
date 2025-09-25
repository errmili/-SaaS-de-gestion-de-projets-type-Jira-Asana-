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
public class FileVersionDto {
    private UUID id;
    private UUID fileId;
    private Integer versionNumber;
    private String storedName;
    private Long fileSize;
    private String humanReadableSize;
    private UUID uploadedBy;
    private LocalDateTime uploadedAt;
    private String comment;
    private Boolean isCurrent;
}