// ===========================================
// BulkTaskUpdateRequest.java - Requête mise à jour en masse
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Task;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkTaskUpdateRequest {

    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<UUID> taskIds;

    // Champs à mettre à jour (null = pas de changement)
    private Task.TaskStatus status;
    private Task.Priority priority;
    private UUID assigneeId;
    private UUID sprintId;
    private String comment; // Commentaire pour l'historique
}