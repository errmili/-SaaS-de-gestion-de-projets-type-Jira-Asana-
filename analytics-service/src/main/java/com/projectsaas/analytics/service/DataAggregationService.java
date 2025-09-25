package com.projectsaas.analytics.service;

import com.projectsaas.analytics.dto.ChartDataDto;
import com.projectsaas.analytics.enums.TimeRange;
import com.projectsaas.analytics.repository.ProjectMetricsRepository;
import com.projectsaas.analytics.repository.TaskMetricsRepository;
import com.projectsaas.analytics.repository.UserActivityRepository;
import com.projectsaas.analytics.repository.NotificationMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataAggregationService {

    private final ProjectMetricsRepository projectMetricsRepository;
    private final TaskMetricsRepository taskMetricsRepository;
    private final UserActivityRepository userActivityRepository;
    private final NotificationMetricsRepository notificationMetricsRepository;

    public List<ChartDataDto> getProjectsOverTime(TimeRange timeRange) {
        log.info("Aggregating projects over time for range: {}", timeRange);

        List<ChartDataDto> data = new ArrayList<>();
        LocalDateTime start = calculateStartDate(timeRange);
        LocalDateTime end = LocalDateTime.now();

        // Generate sample data points - in real implementation, query database
        LocalDateTime current = start;
        int projectCount = 0;

        while (current.isBefore(end)) {
            projectCount += (int) (Math.random() * 3); // Random increment for demo

            data.add(ChartDataDto.builder()
                    .timestamp(current)
                    .label(current.toLocalDate().toString())
                    .value((double) projectCount)
                    .additionalData(Map.of("period", "projects_created"))
                    .build());

            current = current.plus(1, getChronoUnit(timeRange));
        }

        return data;
    }

    public List<ChartDataDto> getTasksVelocity(TimeRange timeRange) {
        log.info("Aggregating tasks velocity for range: {}", timeRange);

        List<ChartDataDto> data = new ArrayList<>();
        LocalDateTime start = calculateStartDate(timeRange);
        LocalDateTime end = LocalDateTime.now();

        LocalDateTime current = start;

        while (current.isBefore(end)) {
            // Sample velocity calculation - replace with real data
            double velocity = 5 + (Math.random() * 10); // 5-15 tasks per period

            data.add(ChartDataDto.builder()
                    .timestamp(current)
                    .label(current.toLocalDate().toString())
                    .value(velocity)
                    .additionalData(Map.of("metric", "tasks_completed"))
                    .build());

            current = current.plus(1, getChronoUnit(timeRange));
        }

        return data;
    }

    public List<ChartDataDto> getUserActivityOverTime(TimeRange timeRange) {
        log.info("Aggregating user activity for range: {}", timeRange);

        List<ChartDataDto> data = new ArrayList<>();
        LocalDateTime start = calculateStartDate(timeRange);
        LocalDateTime end = LocalDateTime.now();

        LocalDateTime current = start;

        while (current.isBefore(end)) {
            // Sample activity calculation - replace with real data
            double activity = 20 + (Math.random() * 30); // 20-50 activities per period

            data.add(ChartDataDto.builder()
                    .timestamp(current)
                    .label(current.toLocalDate().toString())
                    .value(activity)
                    .additionalData(Map.of("metric", "user_activities"))
                    .build());

            current = current.plus(1, getChronoUnit(timeRange));
        }

        return data;
    }

    public List<ChartDataDto> getNotificationMetrics(TimeRange timeRange) {
        log.info("Aggregating notification metrics for range: {}", timeRange);

        List<ChartDataDto> data = new ArrayList<>();
        LocalDateTime since = calculateStartDate(timeRange);

        // Get notification stats by channel
        List<Object[]> channelStats = notificationMetricsRepository.getNotificationsByChannelSince(since);

        for (Object[] stat : channelStats) {
            String channel = (String) stat[0];
            Long count = (Long) stat[1];

            data.add(ChartDataDto.builder()
                    .timestamp(LocalDateTime.now())
                    .label(channel)
                    .value(count.doubleValue())
                    .additionalData(Map.of("channel", channel, "metric", "notifications_sent"))
                    .build());
        }

        // If no data, provide sample data
        if (data.isEmpty()) {
            data.add(ChartDataDto.builder()
                    .label("EMAIL")
                    .value(150.0)
                    .additionalData(Map.of("channel", "EMAIL"))
                    .build());

            data.add(ChartDataDto.builder()
                    .label("WEBSOCKET")
                    .value(89.0)
                    .additionalData(Map.of("channel", "WEBSOCKET"))
                    .build());

            data.add(ChartDataDto.builder()
                    .label("PUSH")
                    .value(45.0)
                    .additionalData(Map.of("channel", "PUSH"))
                    .build());
        }

        return data;
    }

    private LocalDateTime calculateStartDate(TimeRange timeRange) {
        return switch (timeRange) {
            case LAST_24_HOURS -> LocalDateTime.now().minusHours(24);
            case LAST_7_DAYS -> LocalDateTime.now().minusDays(7);
            case LAST_30_DAYS -> LocalDateTime.now().minusDays(30);
            case LAST_3_MONTHS -> LocalDateTime.now().minusMonths(3);
            case LAST_6_MONTHS -> LocalDateTime.now().minusMonths(6);
            case LAST_YEAR -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusDays(30);
        };
    }

    private ChronoUnit getChronoUnit(TimeRange timeRange) {
        return switch (timeRange) {
            case LAST_24_HOURS -> ChronoUnit.HOURS;
            case LAST_7_DAYS -> ChronoUnit.DAYS;
            case LAST_30_DAYS -> ChronoUnit.DAYS;
            case LAST_3_MONTHS -> ChronoUnit.WEEKS;
            case LAST_6_MONTHS -> ChronoUnit.WEEKS;
            case LAST_YEAR -> ChronoUnit.MONTHS;
            default -> ChronoUnit.DAYS;
        };
    }
}