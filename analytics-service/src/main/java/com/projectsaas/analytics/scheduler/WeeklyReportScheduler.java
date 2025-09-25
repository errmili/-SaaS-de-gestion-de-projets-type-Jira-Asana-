package com.projectsaas.analytics.scheduler;

import com.projectsaas.analytics.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final ReportGenerationService reportGenerationService;

    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9:00 AM
    public void generateWeeklyReports() {
        log.info("Starting weekly reports generation");

        try {
            reportGenerationService.generateWeeklyTeamProductivityReport();
            reportGenerationService.generateWeeklyProjectSummary();

            log.info("Weekly reports generated successfully");
        } catch (Exception e) {
            log.error("Failed to generate weekly reports", e);
        }
    }
}
