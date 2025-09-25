package com.projectsaas.analytics.service;

import com.projectsaas.analytics.dto.DashboardDataDto;
import com.projectsaas.analytics.dto.KPISummaryDto;
import com.projectsaas.analytics.dto.ProjectMetricsDto;
import com.projectsaas.analytics.dto.UserProductivityDto;
import com.projectsaas.analytics.enums.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final KPICalculationService kpiCalculationService;
    private final DashboardService dashboardService;
    private final MetricsCollectionService metricsCollectionService;

    public DashboardDataDto getDashboardData(Long userId, TimeRange timeRange) {
        log.info("Getting dashboard data for user: {} with time range: {}", userId, timeRange);

        return dashboardService.generateDashboardData(userId, timeRange);
    }

    public ProjectMetricsDto getProjectMetrics(Long projectId, TimeRange timeRange) {
        log.info("Getting project metrics for project: {} with time range: {}", projectId, timeRange);

        return metricsCollectionService.getProjectMetrics(projectId, timeRange);
    }

    public UserProductivityDto getUserProductivity(Long userId, TimeRange timeRange) {
        log.info("Getting user productivity for user: {} with time range: {}", userId, timeRange);

        return metricsCollectionService.getUserProductivity(userId, timeRange);
    }

    public KPISummaryDto getKPISummary(TimeRange timeRange) {
        log.info("Getting KPI summary with time range: {}", timeRange);

        return kpiCalculationService.calculateKPISummary(timeRange);
    }

    public List<ProjectMetricsDto> getTopProjects(TimeRange timeRange, int limit) {
        return metricsCollectionService.getTopProjects(timeRange, limit);
    }

    public List<UserProductivityDto> getTopUsers(TimeRange timeRange, int limit) {
        return metricsCollectionService.getTopUsers(timeRange, limit);
    }

    // Method for triggering manual metrics calculation
    public void triggerMetricsCalculation() {
        log.info("Triggering manual metrics calculation");
        metricsCollectionService.calculateCurrentMetrics();
    }
}