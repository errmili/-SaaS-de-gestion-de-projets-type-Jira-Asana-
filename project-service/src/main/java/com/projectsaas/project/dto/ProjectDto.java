// ===========================================
// ProjectDto.java - DTO projet
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Project;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {

    private UUID id;
    private String name;
    private String description;
    private String key;
    private Project.ProjectStatus status;
    private Project.Priority priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer memberCount;
    private Integer taskCount;
}