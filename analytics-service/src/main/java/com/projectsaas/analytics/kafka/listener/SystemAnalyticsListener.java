package com.projectsaas.analytics.kafka.listener;


import com.projectsaas.analytics.entity.SystemMetrics;
import com.projectsaas.analytics.repository.SystemMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemAnalyticsListener {

    private final SystemMetricsRepository systemMetricsRepository;

    @KafkaListener(topics = "system.performance", groupId = "analytics-service")
    public void handleSystemPerformance(Map<String, Object> event) {
        log.info("Received system performance event: {}", event);

        try {
            String serviceName = (String) event.get("serviceName");
            String metricName = (String) event.get("metricName");
            Double metricValue = ((Number) event.get("metricValue")).doubleValue();
            String unit = (String) event.getOrDefault("unit", "");
            String tags = (String) event.getOrDefault("tags", "{}");

            SystemMetrics systemMetrics = SystemMetrics.builder()
                    .serviceName(serviceName)
                    .metricName(metricName)
                    .metricValue(metricValue)
                    .unit(unit)
                    .tags(tags)
                    .recordedAt(LocalDateTime.now())
                    .build();

            systemMetricsRepository.save(systemMetrics);

        } catch (Exception e) {
            log.error("Error processing system performance event", e);
        }
    }

    @KafkaListener(topics = "system.error", groupId = "analytics-service")
    public void handleSystemError(Map<String, Object> event) {
        log.info("Received system error event: {}", event);

        try {
            String serviceName = (String) event.get("serviceName");
            String errorType = (String) event.get("errorType");
            String errorMessage = (String) event.get("errorMessage");

            // Record error as a metric
            SystemMetrics errorMetric = SystemMetrics.builder()
                    .serviceName(serviceName)
                    .metricName("error_count")
                    .metricValue(1.0)
                    .unit("count")
                    .tags(String.format("{\"errorType\":\"%s\",\"errorMessage\":\"%s\"}", errorType, errorMessage))
                    .recordedAt(LocalDateTime.now())
                    .build();

            systemMetricsRepository.save(errorMetric);

        } catch (Exception e) {
            log.error("Error processing system error event", e);
        }
    }

    @KafkaListener(topics = "api.request", groupId = "analytics-service")
    public void handleApiRequest(Map<String, Object> event) {
        log.debug("Received API request event: {}", event);

        try {
            String serviceName = (String) event.get("serviceName");
            String endpoint = (String) event.get("endpoint");
            String method = (String) event.get("method");
            Integer responseTime = ((Number) event.get("responseTime")).intValue();
            Integer statusCode = ((Number) event.get("statusCode")).intValue();

            // Record response time metric
            SystemMetrics responseTimeMetric = SystemMetrics.builder()
                    .serviceName(serviceName)
                    .metricName("api_response_time")
                    .metricValue(responseTime.doubleValue())
                    .unit("ms")
                    .tags(String.format("{\"endpoint\":\"%s\",\"method\":\"%s\",\"statusCode\":%d}",
                            endpoint, method, statusCode))
                    .recordedAt(LocalDateTime.now())
                    .build();

            systemMetricsRepository.save(responseTimeMetric);

            // Record request count metric
            SystemMetrics requestCountMetric = SystemMetrics.builder()
                    .serviceName(serviceName)
                    .metricName("api_requests_total")
                    .metricValue(1.0)
                    .unit("count")
                    .tags(String.format("{\"endpoint\":\"%s\",\"method\":\"%s\",\"statusCode\":%d}",
                            endpoint, method, statusCode))
                    .recordedAt(LocalDateTime.now())
                    .build();

            systemMetricsRepository.save(requestCountMetric);

        } catch (Exception e) {
            log.error("Error processing API request event", e);
        }
    }
}
