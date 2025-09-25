package com.projectsaas.analytics.repository;

import com.projectsaas.analytics.entity.NotificationMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationMetricsRepository extends JpaRepository<NotificationMetrics, Long> {

    @Query("SELECT COUNT(nm) FROM NotificationMetrics nm WHERE nm.sentAt >= :since")
    Long countNotificationsSentSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(nm) FROM NotificationMetrics nm WHERE nm.readAt >= :since")
    Long countNotificationsReadSince(@Param("since") LocalDateTime since);

    @Query("SELECT nm.deliveryChannel, COUNT(nm) FROM NotificationMetrics nm WHERE nm.sentAt >= :since GROUP BY nm.deliveryChannel")
    List<Object[]> getNotificationsByChannelSince(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(nm.timeToRead) FROM NotificationMetrics nm WHERE nm.readAt >= :since")
    Double getAverageTimeToReadSince(@Param("since") LocalDateTime since);

    List<NotificationMetrics> findByUserIdAndSentAtBetween(
            Long userId, LocalDateTime start, LocalDateTime end);
}