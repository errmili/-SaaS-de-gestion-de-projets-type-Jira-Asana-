// ===========================================
// AssignTaskRequest.java - Requête d'assignation de tâche
// ===========================================
package com.projectsaas.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTaskRequest {

    @NotNull(message = "Assignee ID is required")
    private UUID assigneeId;

    private String comment; // Commentaire optionnel sur l'assignation
}