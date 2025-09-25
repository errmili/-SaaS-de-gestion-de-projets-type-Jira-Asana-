// ===========================================
// CreateTenantRequest.java - Création d'organisation
// ===========================================

package com.projectsaas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Subdomain is required")
    @Size(min = 2, max = 100, message = "Subdomain must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
            message = "Subdomain must contain only lowercase letters, numbers and hyphens")
    private String subdomain;

    // Informations du premier utilisateur (admin)
    @NotBlank(message = "Admin email is required")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Admin password must be at least 8 characters")
    private String adminPassword;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
}
