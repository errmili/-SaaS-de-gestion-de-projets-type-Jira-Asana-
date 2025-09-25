package com.projectsaas.analytics.repository;

import com.projectsaas.analytics.entity.UserActivity;
import com.projectsaas.analytics.enums.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<UserActivity> findByProjectIdAndActivityDateBetween(
            Long projectId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityDate >= :since ORDER BY ua.activityDate DESC")
    List<UserActivity> findRecentActivities(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT ua.userId) FROM UserActivity ua WHERE ua.activityDate >= :since")
    Long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT ua.userId, COUNT(ua) FROM UserActivity ua WHERE ua.activityDate >= :since GROUP BY ua.userId ORDER BY COUNT(ua) DESC")
    List<Object[]> findMostActiveUsersSince(@Param("since") LocalDateTime since);

    List<UserActivity> findByActivityTypeAndActivityDateBetween(
            MetricType activityType, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("DELETE FROM UserActivity ua WHERE ua.createdAt < :cutoffDate")
    void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
