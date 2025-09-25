package com.projectsaas.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventDto {
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private Long projectId;
    private String projectName;
    private Long assignedUserId;
    private String assignedUserName;
    private String assignedUserEmail;
    private Long assignedByUserId;
    private String assignedByUserName;
    private String status;
    private LocalDateTime deadline;
    private String eventType; // CREATED, UPDATED, ASSIGNED, COMPLETED
    private LocalDateTime timestamp;
}