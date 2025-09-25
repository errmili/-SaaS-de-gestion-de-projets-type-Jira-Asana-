package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPISummaryDto {

    // Project KPIs
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;
    private Double projectCompletionRate;

    // Task KPIs
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer overdueTasks;
    private Double averageTaskDuration;

    // User KPIs
    private Integer totalUsers;
    private Integer activeUsers;
    private Double averageProductivityScore;

    // System KPIs
    private Long totalNotifications;
    private Double notificationReadRate;
    private Long totalFiles;
    private Double systemResponseTime;

    // Trends (vs previous period)
    private Double projectsTrend;
    private Double tasksTrend;
    private Double userActivityTrend;
}