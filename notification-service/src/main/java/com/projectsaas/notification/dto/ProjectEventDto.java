package com.projectsaas.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEventDto {
    private Long projectId;
    private String projectName;
    private String projectDescription;
    private Long ownerId;
    private String ownerName;
    private List<Long> memberIds;
    private List<String> memberEmails;
    private String eventType; // CREATED, UPDATED, MEMBER_ADDED, MEMBER_REMOVED
    private LocalDateTime timestamp;
}