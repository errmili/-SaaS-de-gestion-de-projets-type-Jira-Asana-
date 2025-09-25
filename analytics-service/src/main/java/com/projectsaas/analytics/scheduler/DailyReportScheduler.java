package com.projectsaas.analytics.scheduler;

import com.projectsaas.analytics.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReportScheduler {

    private final ReportGenerationService reportGenerationService;

    @Scheduled(cron = "0 0 8 * * *") // Every day at 8:00 AM
    public void generateDailyReports() {
        log.info("Starting daily reports generation");

        try {
            reportGenerationService.generateDailyExecutiveSummary();
            reportGenerationService.generateDailyProjectReports();

            log.info("Daily reports generated successfully");
        } catch (Exception e) {
            log.error("Failed to generate daily reports", e);
        }
    }

    @Scheduled(cron = "0 30 7 * * *") // Every day at 7:30 AM
    public void generateDailyAlerts() {
        log.info("Generating daily alerts");

        try {
            reportGenerationService.generateOverdueTasksAlert();
            reportGenerationService.generateProductivityAlerts();

            log.info("Daily alerts generated successfully");
        } catch (Exception e) {
            log.error("Failed to generate daily alerts", e);
        }
    }
}
