package com.projectsaas.analytics.controller;


import com.projectsaas.analytics.dto.ReportRequestDto;
import com.projectsaas.analytics.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateReport(@RequestBody ReportRequestDto request) {
        String reportPath = reportGenerationService.generateReport(request);

        return ResponseEntity.ok(Map.of(
                "reportId", extractReportId(reportPath),
                "reportPath", reportPath,
                "status", "generated"
        ));
    }

    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable String reportId) {
        // In real implementation, resolve report path from ID
        String reportPath = "/path/to/reports/" + reportId;
        File file = new File(reportPath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getAvailableTemplates() {
        Map<String, Object> templates = Map.of(
                "PROJECT_SUMMARY", "Project Summary Report",
                "USER_PRODUCTIVITY", "User Productivity Report",
                "TEAM_PERFORMANCE", "Team Performance Report",
                "EXECUTIVE_DASHBOARD", "Executive Dashboard",
                "WEEKLY_SUMMARY", "Weekly Summary",
                "MONTHLY_OVERVIEW", "Monthly Overview"
        );

        return ResponseEntity.ok(Map.of("templates", templates));
    }

    private String extractReportId(String reportPath) {
        // Extract report ID from file path
        String fileName = new File(reportPath).getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}