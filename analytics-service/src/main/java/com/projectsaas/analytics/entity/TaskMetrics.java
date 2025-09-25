package com.projectsaas.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_metrics", indexes = {
        @Index(name = "idx_task_id", columnList = "taskId"),
        @Index(name = "idx_project_id", columnList = "projectId"),
        @Index(name = "idx_assigned_user", columnList = "assignedUserId"),
        @Index(name = "idx_created_date", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private String taskTitle;

    private Long projectId;
    private String projectName;

    private Long assignedUserId;
    private String assignedUserName;

    private Long createdByUserId;
    private String createdByUserName;

    // Temporal data
    private LocalDateTime taskCreatedAt;
    private LocalDateTime taskAssignedAt;
    private LocalDateTime taskStartedAt;
    private LocalDateTime taskCompletedAt;
    private LocalDateTime taskDeadline;

    // Status
    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    // Calculated metrics
    private Long timeToAssignment; // minutes from creation to assignment
    private Long timeToStart; // minutes from assignment to start
    private Long timeToCompletion; // minutes from start to completion
    private Long totalDuration; // minutes from creation to completion

    private Boolean isOverdue;
    private Boolean isOnTime;
    private Long daysOverdue;

    // Activity metrics
    private Integer commentsCount;
    private Integer filesAttached;
    private Integer statusChanges;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}