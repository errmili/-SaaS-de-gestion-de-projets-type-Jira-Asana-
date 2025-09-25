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
public class MetricsEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDailyMetricsCalculated(String entityType, Long entityId, Map<String, Object> metrics) {
        Map<String, Object> event = Map.of(
                "entityType", entityType, // "project", "user", "system"
                "entityId", entityId,
                "metrics", metrics,
                "date", LocalDateTime.now().toLocalDate(),
                "calculatedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.daily.metrics", event);
        log.info("Sent daily metrics calculated event for {} {}: {}", entityType, entityId, event);
    }

    public void sendTrendAnalysis(String trendType, String entityType, Long entityId,
                                  Double currentValue, Double previousValue, Double changePercent) {
        Map<String, Object> event = Map.of(
                "trendType", trendType, // "productivity", "velocity", "completion_rate"
                "entityType", entityType,
                "entityId", entityId,
                "currentValue", currentValue,
                "previousValue", previousValue,
                "changePercent", changePercent,
                "trend", changePercent > 0 ? "INCREASING" : changePercent < 0 ? "DECREASING" : "STABLE",
                "analyzedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.trend.analysis", event);
        log.info("Sent trend analysis event: {}", event);
    }

    public void sendPerformanceBenchmark(String benchmarkType, Map<String, Object> benchmarkData) {
        Map<String, Object> event = Map.of(
                "benchmarkType", benchmarkType, // "team_performance", "project_efficiency"
                "benchmarkData", benchmarkData,
                "calculatedAt", LocalDateTime.now(),
                "service", "analytics-service"
        );

        kafkaTemplate.send("analytics.benchmark.calculated", event);
        log.info("Sent performance benchmark event: {}", event);
    }
}