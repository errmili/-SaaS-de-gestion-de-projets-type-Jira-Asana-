// ===========================================
// TaskDto.java - DTO tâche
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Task;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private String taskKey;
    private Task.TaskStatus status;
    private Task.Priority priority;
    private Task.TaskType taskType;
    private Integer storyPoints;
    private UUID assigneeId;
    private UUID reporterId;
    private LocalDateTime dueDate;
    private UUID sprintId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer commentCount;

    // Informations additionnelles (pour l'affichage)
    private String assigneeName;
    private String reporterName;
    private String projectName;
    private String sprintName;
}
