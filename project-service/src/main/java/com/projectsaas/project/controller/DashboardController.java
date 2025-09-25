package com.projectsaas.project.controller;

import com.projectsaas.project.dto.ApiResponse;
import com.projectsaas.project.dto.DashboardDto;
import com.projectsaas.project.dto.ProjectStatsDto;
import com.projectsaas.project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting dashboard data");

        String token = authHeader.substring(7);
        DashboardDto dashboard = dashboardService.getDashboardData(token);

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data retrieved successfully", dashboard)
        );
    }

    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<ApiResponse<ProjectStatsDto>> getProjectStats(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting stats for project: {}", projectId);

        String token = authHeader.substring(7);
        ProjectStatsDto stats = dashboardService.getProjectStats(projectId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Project stats retrieved successfully", stats)
        );
    }
}
