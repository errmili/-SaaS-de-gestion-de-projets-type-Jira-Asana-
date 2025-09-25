package com.projectsaas.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics", indexes = {
        @Index(name = "idx_metric_name", columnList = "metricName"),
        @Index(name = "idx_recorded_at", columnList = "recordedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String metricName;

    @Column(nullable = false)
    private Double metricValue;

    private String unit; // requests/sec, MB, %, etc.
    private String serviceName; // auth-service, project-service, etc.

    @Column(columnDefinition = "TEXT")
    private String tags; // JSON string for additional dimensions

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}