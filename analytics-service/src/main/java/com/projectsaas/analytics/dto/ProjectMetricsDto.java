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
public class ProjectMetricsDto {
    private Long projectId;
    private String projectName;
    private String ownerName;

    // Current stats
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer pendingTasks;
    private Integer overdueTasks;
    private Integer totalMembers;

    // Calculated metrics
    private Double completionRate;
    private Double velocityTasksPerDay;
    private Double teamProductivityScore;

    // Dates
    private LocalDateTime projectStartDate;
    private LocalDateTime lastActivity;

    // Trends (compared to previous period)
    private Double tasksCompletedTrend;
    private Double velocityTrend;
}