package com.projectsaas.analytics.service;
import com.projectsaas.analytics.dto.*;
import com.projectsaas.analytics.enums.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final KPICalculationService kpiCalculationService;
    private final MetricsCollectionService metricsCollectionService;
    private final DataAggregationService dataAggregationService;

    public DashboardDataDto generateDashboardData(Long userId, TimeRange timeRange) {
        log.info("Generating dashboard data for user: {} with time range: {}", userId, timeRange);

        // Get KPI summary
        KPISummaryDto kpiSummary = kpiCalculationService.calculateKPISummary(timeRange);

        // Get chart data
        List<ChartDataDto> projectsOverTime = dataAggregationService.getProjectsOverTime(timeRange);
        List<ChartDataDto> tasksVelocity = dataAggregationService.getTasksVelocity(timeRange);
        List<ChartDataDto> userActivity = dataAggregationService.getUserActivityOverTime(timeRange);
        List<ChartDataDto> notificationMetrics = dataAggregationService.getNotificationMetrics(timeRange);

        // Get top performers
        List<UserProductivityDto> topUsers = metricsCollectionService.getTopUsers(timeRange, 5);
        List<ProjectMetricsDto> topProjects = metricsCollectionService.getTopProjects(timeRange, 5);

        // Get recent activities
        List<RecentActivityDto> recentActivities = getRecentActivities(10);

        // Get alerts
        List<AlertDto> alerts = generateAlerts();

        return DashboardDataDto.builder()
                .kpiSummary(kpiSummary)
                .projectsOverTime(projectsOverTime)
                .tasksVelocity(tasksVelocity)
                .userActivity(userActivity)
                .notificationMetrics(notificationMetrics)
                .topUsers(topUsers)
                .topProjects(topProjects)
                .recentActivities(recentActivities)
                .alerts(alerts)
                .build();
    }

    private List<RecentActivityDto> getRecentActivities(int limit) {
        List<RecentActivityDto> activities = new ArrayList<>();

        // Sample recent activities - in real implementation, fetch from user_activity table
        activities.add(RecentActivityDto.builder()
                .activityType("TASK_COMPLETED")
                .description("Alice completed 'User Authentication'")
                .userName("Alice Johnson")
                .projectName("E-commerce Platform")
                .timestamp(LocalDateTime.now().minusHours(2))
                .icon("✅")
                .color("green")
                .build());

        activities.add(RecentActivityDto.builder()
                .activityType("PROJECT_CREATED")
                .description("John created new project 'Mobile App'")
                .userName("John Doe")
                .projectName("Mobile App")
                .timestamp(LocalDateTime.now().minusHours(4))
                .icon("🚀")
                .color("blue")
                .build());

        return activities;
    }

    private List<AlertDto> generateAlerts() {
        List<AlertDto> alerts = new ArrayList<>();

        // Sample alerts - in real implementation, generate based on metrics
        alerts.add(AlertDto.builder()
                .alertType("WARNING")
                .title("Tasks Overdue")
                .message("5 tasks are overdue and need attention")
                .severity("HIGH")
                .createdAt(LocalDateTime.now().minusHours(1))
                .isRead(false)
                .build());

        alerts.add(AlertDto.builder()
                .alertType("INFO")
                .title("Productivity Increase")
                .message("Team productivity increased by 15% this week")
                .severity("LOW")
                .createdAt(LocalDateTime.now().minusHours(3))
                .isRead(false)
                .build());

        return alerts;
    }
}
