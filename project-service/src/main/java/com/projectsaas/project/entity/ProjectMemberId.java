package com.projectsaas.project.entity;

// ===========================================
// ProjectMemberId.java - Clé composite
// ===========================================
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberId implements Serializable {
    private UUID project;
    private UUID userId;
}