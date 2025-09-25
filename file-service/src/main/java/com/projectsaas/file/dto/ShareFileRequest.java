package com.projectsaas.file.dto;

import com.projectsaas.file.entity.FileShare;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareFileRequest {

    private UUID sharedWith; // null pour partage public

    @NotNull(message = "Le niveau de permission est requis")
    private FileShare.SharePermission permission;

    private LocalDateTime expiresAt;

    @Size(max = 500, message = "Le message ne peut pas dépasser 500 caractères")
    private String message;
}