package com.projectsaas.analytics.scheduler;

import com.projectsaas.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsCleanupScheduler {

    private final UserActivityRepository userActivityRepository;
    private final SystemMetricsRepository systemMetricsRepository;
    private final NotificationMetricsRepository notificationMetricsRepository;

    @Value("${analytics.retention.raw-data-days:90}")
    private int rawDataRetentionDays;

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    public void cleanupOldMetrics() {
        log.info("Starting metrics cleanup");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(rawDataRetentionDays);

        try {
            // Cleanup old user activities
            userActivityRepository.deleteByCreatedAtBefore(cutoffDate);

            // Cleanup old system metrics
            systemMetricsRepository.deleteByCreatedAtBefore(cutoffDate);

            log.info("Metrics cleanup completed successfully");
        } catch (Exception e) {
            log.error("Failed to cleanup old metrics", e);
        }
    }
}