// ===========================================
// AddMemberRequest.java - Requête ajout membre
// ===========================================
package com.projectsaas.project.dto;

import com.projectsaas.project.entity.ProjectMember;
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
public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private ProjectMember.MemberRole role = ProjectMember.MemberRole.MEMBER;
}
