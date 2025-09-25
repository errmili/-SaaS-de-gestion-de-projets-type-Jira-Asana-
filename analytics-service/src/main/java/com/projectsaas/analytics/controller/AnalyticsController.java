package com.projectsaas.analytics.controller;

import com.projectsaas.analytics.dto.*;
import com.projectsaas.analytics.enums.TimeRange;
import com.projectsaas.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardDataDto> getDashboard(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "LAST_7_DAYS") TimeRange timeRange) {

        DashboardDataDto dashboard = analyticsService.getDashboardData(userId, timeRange);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/project/{projectId}/metrics")
    public ResponseEntity<ProjectMetricsDto> getProjectMetrics(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange) {

        ProjectMetricsDto metrics = analyticsService.getProjectMetrics(projectId, timeRange);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/user/{userId}/productivity")
    public ResponseEntity<UserProductivityDto> getUserProductivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange) {

        UserProductivityDto productivity = analyticsService.getUserProductivity(userId, timeRange);
        return ResponseEntity.ok(productivity);
    }

    @GetMapping("/kpis")
    public ResponseEntity<KPISummaryDto> getKPISummary(
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange) {

        KPISummaryDto kpis = analyticsService.getKPISummary(timeRange);
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/projects/top")
    public ResponseEntity<List<ProjectMetricsDto>> getTopProjects(
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange,
            @RequestParam(defaultValue = "10") int limit) {

        List<ProjectMetricsDto> projects = analyticsService.getTopProjects(timeRange, limit);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/users/top")
    public ResponseEntity<List<UserProductivityDto>> getTopUsers(
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange,
            @RequestParam(defaultValue = "10") int limit) {

        List<UserProductivityDto> users = analyticsService.getTopUsers(timeRange, limit);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, String>> triggerCalculation() {
        analyticsService.triggerMetricsCalculation();
        return ResponseEntity.ok(Map.of("message", "Metrics calculation triggered"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "analytics-service",
                "version", "1.0.0"
        ));
    }
}