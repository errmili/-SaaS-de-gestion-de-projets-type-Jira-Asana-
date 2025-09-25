package com.projectsaas.analytics.service;

import com.projectsaas.analytics.dto.ReportRequestDto;
import com.projectsaas.analytics.enums.ReportType;
import com.projectsaas.analytics.enums.TimeRange;
import com.projectsaas.analytics.util.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final AnalyticsService analyticsService;
    private final ReportGenerator reportGenerator;

    public String generateReport(ReportRequestDto request) {
        log.info("Generating report: {} for time range: {}", request.getReportType(), request.getTimeRange());

        try {
            return switch (request.getReportType()) {
                case PROJECT_SUMMARY -> generateProjectSummaryReport(request);
                case USER_PRODUCTIVITY -> generateUserProductivityReport(request);
                case TEAM_PERFORMANCE -> generateTeamPerformanceReport(request);
                case EXECUTIVE_DASHBOARD -> generateExecutiveDashboardReport(request);
                case WEEKLY_SUMMARY -> generateWeeklySummaryReport(request);
                case MONTHLY_OVERVIEW -> generateMonthlyOverviewReport(request);
                default -> throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
            };
        } catch (Exception e) {
            log.error("Failed to generate report", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }

    public void generateDailyExecutiveSummary() {
        log.info("Generating daily executive summary");

        ReportRequestDto request = ReportRequestDto.builder()
                .reportType(ReportType.EXECUTIVE_DASHBOARD)
                .timeRange(TimeRange.LAST_24_HOURS)
                .format("PDF")
                .includeCharts(true)
                .sendByEmail(true)
                .recipientEmail("executives@projectsaas.com")
                .build();

        generateReport(request);
    }

    public void generateDailyProjectReports() {
        log.info("Generating daily project reports");

        // Get all active projects and generate reports for each
        // Implementation would iterate through projects
    }

    public void generateOverdueTasksAlert() {
        log.info("Generating overdue tasks alert");

        // Implementation to check for overdue tasks and send alerts
    }

    public void generateProductivityAlerts() {
        log.info("Generating productivity alerts");

        // Implementation to check for productivity issues and send alerts
    }

    public void generateWeeklyTeamProductivityReport() {
        log.info("Generating weekly team productivity report");

        ReportRequestDto request = ReportRequestDto.builder()
                .reportType(ReportType.TEAM_PERFORMANCE)
                .timeRange(TimeRange.LAST_7_DAYS)
                .format("PDF")
                .includeCharts(true)
                .sendByEmail(true)
                .recipientEmail("managers@projectsaas.com")
                .build();

        generateReport(request);
    }

    public void generateWeeklyProjectSummary() {
        log.info("Generating weekly project summary");

        ReportRequestDto request = ReportRequestDto.builder()
                .reportType(ReportType.WEEKLY_SUMMARY)
                .timeRange(TimeRange.LAST_7_DAYS)
                .format("EXCEL")
                .includeCharts(false)
                .sendByEmail(true)
                .recipientEmail("project-managers@projectsaas.com")
                .build();

        generateReport(request);
    }

    private String generateProjectSummaryReport(ReportRequestDto request) {
        var kpis = analyticsService.getKPISummary(request.getTimeRange());
        var topProjects = analyticsService.getTopProjects(request.getTimeRange(), 10);

        Map<String, Object> data = Map.of(
                "kpis", kpis,
                "projects", topProjects,
                "generatedAt", LocalDateTime.now(),
                "timeRange", request.getTimeRange()
        );

        return reportGenerator.generateReport("project-summary", data, request.getFormat());
    }

    private String generateUserProductivityReport(ReportRequestDto request) {
        var topUsers = analyticsService.getTopUsers(request.getTimeRange(), 20);

        Map<String, Object> data = Map.of(
                "users", topUsers,
                "generatedAt", LocalDateTime.now(),
                "timeRange", request.getTimeRange()
        );

        return reportGenerator.generateReport("user-productivity", data, request.getFormat());
    }

    private String generateTeamPerformanceReport(ReportRequestDto request) {
        var kpis = analyticsService.getKPISummary(request.getTimeRange());
        var topUsers = analyticsService.getTopUsers(request.getTimeRange(), 10);
        var topProjects = analyticsService.getTopProjects(request.getTimeRange(), 5);

        Map<String, Object> data = Map.of(
                "kpis", kpis,
                "users", topUsers,
                "projects", topProjects,
                "generatedAt", LocalDateTime.now(),
                "timeRange", request.getTimeRange()
        );

        return reportGenerator.generateReport("team-performance", data, request.getFormat());
    }

    private String generateExecutiveDashboardReport(ReportRequestDto request) {
        var dashboard = analyticsService.getDashboardData(null, request.getTimeRange());

        Map<String, Object> data = Map.of(
                "dashboard", dashboard,
                "generatedAt", LocalDateTime.now(),
                "timeRange", request.getTimeRange()
        );

        return reportGenerator.generateReport("executive-dashboard", data, request.getFormat());
    }

    private String generateWeeklySummaryReport(ReportRequestDto request) {
        return generateProjectSummaryReport(request);
    }

    private String generateMonthlyOverviewReport(ReportRequestDto request) {
        return generateExecutiveDashboardReport(request);
    }
}