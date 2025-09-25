// ===========================================
// ChangeStatusRequest.java - Requête changement de statut
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Task;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeStatusRequest {

    @NotNull(message = "New status is required")
    private Task.TaskStatus newStatus;

    private String comment; // Commentaire optionnel sur le changement
}