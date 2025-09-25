// ===========================================
// CreateProjectRequest.java - Requête création projet
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9]*$", message = "Project key must start with a letter and contain only uppercase letters and numbers")
    private String key;

    private Project.Priority priority;
    private LocalDate startDate;
    private LocalDate endDate;
}