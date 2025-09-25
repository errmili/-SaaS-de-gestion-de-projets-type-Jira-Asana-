package com.projectsaas.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_metrics", indexes = {
        @Index(name = "idx_project_id", columnList = "projectId"),
        @Index(name = "idx_created_date", columnList = "createdAt"),
        @Index(name = "idx_project_date", columnList = "projectId, createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String projectName;

    private Long ownerId;
    private String ownerName;

    // Métriques numériques
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer pendingTasks;
    private Integer overdueTasks;

    private Integer totalMembers;
    private Integer activeMembers;

    private Long totalFilesUploaded;
    private Long totalFilesSizeMB;

    private Integer notificationsSent;

    // Métriques temporelles
    private LocalDateTime projectStartDate;
    private LocalDateTime projectEndDate;
    private LocalDateTime lastActivity;

    // Métriques calculées
    private Double completionRate; // percentage
    private Double velocityTasksPerDay;
    private Double averageTaskDuration; // in hours
    private Double teamProductivityScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}