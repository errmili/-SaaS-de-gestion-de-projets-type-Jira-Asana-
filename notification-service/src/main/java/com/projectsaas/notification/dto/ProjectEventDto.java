package com.projectsaas.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEventDto {

    private Long projectId;
    private String projectName;
    private Long ownerId;
    private String ownerName;
    private List<Long> memberIds;
    private List<String> memberEmails;
    private String eventType; // MEMBER_ADDED, PROJECT_UPDATED, etc.
}