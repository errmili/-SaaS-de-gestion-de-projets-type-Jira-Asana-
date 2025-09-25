package com.projectsaas.analytics.controller;

import com.projectsaas.analytics.dto.ChartDataDto;
import com.projectsaas.analytics.enums.TimeRange;
import com.projectsaas.analytics.service.MetricsCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MetricsController {

    private final MetricsCollectionService metricsService;

    @GetMapping("/projects/summary")
    public ResponseEntity<Map<String, Object>> getProjectsSummary(
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange) {

        // Implementation coming in the service
        Map<String, Object> summary = Map.of(
                "totalProjects", 0,
                "activeProjects", 0,
                "completedProjects", 0,
                "averageCompletionRate", 0.0
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/tasks/velocity")
    public ResponseEntity<List<ChartDataDto>> getTasksVelocity(
            @RequestParam(defaultValue = "LAST_30_DAYS") TimeRange timeRange) {

        // Implementation coming in the service
        List<ChartDataDto> velocity = List.of();

        return ResponseEntity.ok(velocity);
    }

    @GetMapping("/users/activity")
    public ResponseEntity<List<ChartDataDto>> getUsersActivity(
            @RequestParam(defaultValue = "LAST_7_DAYS") TimeRange timeRange) {

        // Implementation coming in the service
        List<ChartDataDto> activity = List.of();

        return ResponseEntity.ok(activity);
    }

    @GetMapping("/system/performance")
    public ResponseEntity<Map<String, Object>> getSystemPerformance() {

        Map<String, Object> performance = Map.of(
                "avgResponseTime", 150.0,
                "totalRequests", 1000L,
                "errorRate", 0.1,
                "uptime", 99.9
        );

        return ResponseEntity.ok(performance);
    }
}