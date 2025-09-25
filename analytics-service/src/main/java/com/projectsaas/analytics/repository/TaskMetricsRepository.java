package com.projectsaas.analytics.repository;

import com.projectsaas.analytics.entity.TaskMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskMetricsRepository extends JpaRepository<TaskMetrics, Long> {

    List<TaskMetrics> findByProjectIdOrderByTaskCreatedAtDesc(Long projectId);

    List<TaskMetrics> findByAssignedUserIdOrderByTaskCreatedAtDesc(Long userId);

    List<TaskMetrics> findByTaskId(Long taskId);

    @Query("SELECT tm FROM TaskMetrics tm WHERE tm.taskCreatedAt >= :since AND tm.status = 'COMPLETED'")
    List<TaskMetrics> findCompletedTasksSince(@Param("since") LocalDateTime since);

    @Query("SELECT tm FROM TaskMetrics tm WHERE tm.taskDeadline < CURRENT_TIMESTAMP AND tm.status != 'COMPLETED'")
    List<TaskMetrics> findOverdueTasks();

    @Query("SELECT AVG(tm.totalDuration) FROM TaskMetrics tm WHERE tm.taskCompletedAt >= :since")
    Double getAverageTaskDurationSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(tm) FROM TaskMetrics tm WHERE tm.assignedUserId = :userId AND tm.taskCreatedAt >= :since")
    Long countTasksAssignedToUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(tm) FROM TaskMetrics tm WHERE tm.assignedUserId = :userId AND tm.status = 'COMPLETED' AND tm.taskCompletedAt >= :since")
    Long countTasksCompletedByUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Overloaded method to get total counts when userId is null
    @Query("SELECT COUNT(tm) FROM TaskMetrics tm WHERE tm.taskCreatedAt >= :since")
    Long countAllTasksSince(@Param("since") LocalDateTime since);
}