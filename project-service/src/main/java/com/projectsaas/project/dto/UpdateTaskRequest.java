// ===========================================
// UpdateTaskRequest.java - Requête mise à jour tâche
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Task;
import jakarta.validation.constraints.Size;
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
public class UpdateTaskRequest {

    @Size(min = 2, max = 255, message = "Task title must be between 2 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Task.TaskStatus status;
    private Task.Priority priority;
    private Task.TaskType taskType;
    private Integer storyPoints;
    private UUID assigneeId;
    private LocalDateTime dueDate;
    private UUID sprintId;
}