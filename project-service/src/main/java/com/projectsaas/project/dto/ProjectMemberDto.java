// ===========================================
// ProjectMemberDto.java - DTO membre de projet
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.ProjectMember;
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
public class ProjectMemberDto {

    private UUID projectId;
    private UUID userId;
    private ProjectMember.MemberRole role;
    private LocalDateTime joinedAt;

    // Informations utilisateur
    private String userName;
    private String userEmail;
    private String userAvatarUrl;
}