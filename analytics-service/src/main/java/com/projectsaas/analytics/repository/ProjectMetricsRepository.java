package com.projectsaas.analytics.repository;

import com.projectsaas.analytics.entity.ProjectMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, Long> {

    Optional<ProjectMetrics> findByProjectIdAndCreatedAtBetween(
            Long projectId, LocalDateTime start, LocalDateTime end);

    List<ProjectMetrics> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ProjectMetrics> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Query("SELECT pm FROM ProjectMetrics pm WHERE pm.createdAt >= :since ORDER BY pm.teamProductivityScore DESC")
    List<ProjectMetrics> findTopProjectsBySince(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(pm.completionRate) FROM ProjectMetrics pm WHERE pm.createdAt >= :since")
    Double getAverageCompletionRateSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT pm.projectId) FROM ProjectMetrics pm WHERE pm.createdAt >= :since")
    Long countActiveProjectsSince(@Param("since") LocalDateTime since);
}