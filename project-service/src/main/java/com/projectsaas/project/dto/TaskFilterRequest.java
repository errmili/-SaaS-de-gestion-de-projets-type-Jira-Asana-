// ===========================================
// TaskFilterRequest.java - Filtres de recherche de tâches
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Task;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterRequest {

    private List<UUID> projectIds;
    private List<Task.TaskStatus> statuses;
    private List<Task.Priority> priorities;
    private List<Task.TaskType> taskTypes;
    private List<UUID> assigneeIds;
    private List<UUID> reporterIds;
    private UUID sprintId;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private String searchTerm;
    private Boolean overdue;
}