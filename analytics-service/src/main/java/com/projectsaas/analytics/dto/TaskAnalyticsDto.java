package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnalyticsDto {
    private Long taskId;
    private String taskTitle;
    private String projectName;
    private String assignedUserName;
    private String status;
    private String priority;

    // Timing analysis
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadline;

    // Performance metrics
    private Long timeToAssignment; // minutes
    private Long timeToCompletion; // minutes
    private Boolean isOverdue;
    private Integer daysOverdue;

    // Efficiency indicators
    private String efficiencyRating; // EXCELLENT, GOOD, AVERAGE, POOR
    private Boolean metDeadline;
}