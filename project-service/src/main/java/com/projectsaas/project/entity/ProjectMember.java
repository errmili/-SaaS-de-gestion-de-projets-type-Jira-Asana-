package com.projectsaas.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProjectMemberId.class)
public class ProjectMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Id
    @Column(name = "user_id")
    private UUID userId; // From Auth Service

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Enum
    public enum MemberRole {
        OWNER, ADMIN, MEMBER, VIEWER
    }
}
