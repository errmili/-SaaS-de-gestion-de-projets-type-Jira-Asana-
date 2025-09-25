// ===========================================
// TaskCommentDto.java - DTO commentaire de tâche
// ===========================================
package com.projectsaas.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentDto {

    private UUID id;
    private UUID taskId;
    private UUID authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Informations additionnelles
    private String authorName;
    private String authorAvatarUrl;
}