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
    private String assignedUserEmail;
    private String assignedUserName;
    private Long assignedByUserId;
    private String assignedByUserName;
    private String status;
    private String priority;
    private LocalDateTime deadline;
    private String eventType; // ASSIGNED, UPDATED, DEADLINE_REMINDER
}