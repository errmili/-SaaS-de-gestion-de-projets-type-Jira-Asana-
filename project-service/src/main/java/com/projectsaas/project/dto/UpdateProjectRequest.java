// ===========================================
// UpdateProjectRequest.java - Requête mise à jour projet
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Project;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Project.ProjectStatus status;
    private Project.Priority priority;
    private LocalDate startDate;
    private LocalDate endDate;
}