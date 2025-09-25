// ===========================================
// CreateSprintRequest.java - Requête création sprint
// ===========================================
package com.projectsaas.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSprintRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Sprint name is required")
    @Size(min = 2, max = 255, message = "Sprint name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Goal must not exceed 1000 characters")
    private String goal;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}