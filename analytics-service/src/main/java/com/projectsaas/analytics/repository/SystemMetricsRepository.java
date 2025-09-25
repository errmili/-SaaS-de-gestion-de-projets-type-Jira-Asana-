package com.projectsaas.analytics.repository;
import com.projectsaas.analytics.entity.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {

    List<SystemMetrics> findByMetricNameAndRecordedAtBetween(
            String metricName, LocalDateTime start, LocalDateTime end);

    List<SystemMetrics> findByServiceNameAndRecordedAtBetween(
            String serviceName, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(sm.metricValue) FROM SystemMetrics sm WHERE sm.metricName = :metricName AND sm.recordedAt >= :since")
    Double getAverageMetricValueSince(@Param("metricName") String metricName, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM SystemMetrics sm WHERE sm.createdAt < :cutoffDate")
    void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
