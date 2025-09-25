// ===========================================
// SprintDto.java - DTO sprint
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Sprint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintDto {

    private UUID id;
    private UUID projectId;
    private String name;
    private String goal;
    private Sprint.SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer taskCount;
    private Integer completedTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;

    // Informations additionnelles
    private String projectName;
    private String createdByName;
    private List<TaskDto> tasks;
}