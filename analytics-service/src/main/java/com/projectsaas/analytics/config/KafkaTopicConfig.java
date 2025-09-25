package com.projectsaas.analytics.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // Topics que Analytics produit
    @Bean
    public NewTopic analyticsKpiCalculatedTopic() {
        return new NewTopic("analytics.kpi.calculated", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsReportGeneratedTopic() {
        return new NewTopic("analytics.report.generated", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsAlertTriggeredTopic() {
        return new NewTopic("analytics.alert.triggered", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsAnomalyDetectedTopic() {
        return new NewTopic("analytics.anomaly.detected", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsProductivityAlertTopic() {
        return new NewTopic("analytics.productivity.alert", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsProjectAlertTopic() {
        return new NewTopic("analytics.project.alert", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsDailyMetricsTopic() {
        return new NewTopic("analytics.daily.metrics", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsTrendAnalysisTopic() {
        return new NewTopic("analytics.trend.analysis", 3, (short) 1);
    }

    @Bean
    public NewTopic analyticsBenchmarkTopic() {
        return new NewTopic("analytics.benchmark.calculated", 3, (short) 1);
    }

    // Topics pour les systèmes externes (optionnels si pas encore créés)
    @Bean
    public NewTopic commentAddedTopic() {
        return new NewTopic("comment.added", 3, (short) 1);
    }

    @Bean
    public NewTopic mentionCreatedTopic() {
        return new NewTopic("mention.created", 3, (short) 1);
    }

    @Bean
    public NewTopic apiRequestTopic() {
        return new NewTopic("api.request", 3, (short) 1);
    }

    @Bean
    public NewTopic systemErrorTopic() {
        return new NewTopic("system.error", 3, (short) 1);
    }
}