package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDto {

    // High-level KPIs
    private KPISummaryDto kpiSummary;

    // Charts data
    private List<ChartDataDto> projectsOverTime;
    private List<ChartDataDto> tasksVelocity;
    private List<ChartDataDto> userActivity;
    private List<ChartDataDto> notificationMetrics;

    // Top performers
    private List<UserProductivityDto> topUsers;
    private List<ProjectMetricsDto> topProjects;

    // Recent activity
    private List<RecentActivityDto> recentActivities;

    // Alerts
    private List<AlertDto> alerts;
}