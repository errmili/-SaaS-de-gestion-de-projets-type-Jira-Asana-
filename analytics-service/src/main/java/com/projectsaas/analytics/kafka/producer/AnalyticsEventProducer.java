package com.projectsaas.analytics.kafka.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendKpiCalculated(String kpiType, Double value, String timeRange) {
        Map<String, Object> event = Map.of(
                "kpiType", kpiType,
                "value", value,
                "timeRange", timeRange,
                "calculatedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.kpi.calculated", event);
        log.info("Sent KPI calculated event: {}", event);
    }

    public void sendReportGenerated(String reportType, String reportPath, String format) {
        Map<String, Object> event = Map.of(
                "reportType", reportType,
                "reportPath", reportPath,
                "format", format,
                "generatedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.report.generated", event);
        log.info("Sent report generated event: {}", event);
    }

    public void sendAlertTriggered(String alertType, String title, String message, String severity) {
        Map<String, Object> event = Map.of(
                "alertType", alertType,
                "title", title,
                "message", message,
                "severity", severity,
                "triggeredAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.alert.triggered", event);
        log.info("Sent alert triggered event: {}", event);
    }

    public void sendAnomalyDetected(String anomalyType, String description, Map<String, Object> data) {
        Map<String, Object> event = Map.of(
                "anomalyType", anomalyType,
                "description", description,
                "data", data,
                "detectedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.anomaly.detected", event);
        log.info("Sent anomaly detected event: {}", event);
    }

    public void sendProductivityAlert(Long userId, String userName, String alertType, Double currentScore, Double previousScore) {
        Map<String, Object> event = Map.of(
                "userId", userId,
                "userName", userName,
                "alertType", alertType,
                "currentScore", currentScore,
                "previousScore", previousScore,
                "change", currentScore - previousScore,
                "alertedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.productivity.alert", event);
        log.info("Sent productivity alert event: {}", event);
    }

    public void sendProjectAlert(Long projectId, String projectName, String alertType, String message) {
        Map<String, Object> event = Map.of(
                "projectId", projectId,
                "projectName", projectName,
                "alertType", alertType,
                "message", message,
                "alertedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.project.alert", event);
        log.info("Sent project alert event: {}", event);
    }

    public void sendSystemMetric(String serviceName, String metricName, Double value, String unit) {
        Map<String, Object> event = Map.of(
                "serviceName", serviceName,
                "metricName", metricName,
                "metricValue", value,
                "unit", unit,
                "recordedAt", LocalDateTime.now(),
                "source", "analytics-service"
        );

        kafkaTemplate.send("system.performance", event);
        log.debug("Sent system metric event: {}", event);
    }
}