package com.projectsaas.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderRequest {

    @NotBlank(message = "Le nom du dossier est requis")
    @Size(min = 1, max = 255, message = "Le nom doit faire entre 1 et 255 caractères")
    private String name;

    private UUID parentId;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
}