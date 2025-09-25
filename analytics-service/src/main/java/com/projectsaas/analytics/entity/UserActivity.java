package com.projectsaas.analytics.entity;

import com.projectsaas.analytics.enums.MetricType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_activity_date", columnList = "activityDate"),
        @Index(name = "idx_user_date", columnList = "userId, activityDate"),
        @Index(name = "idx_activity_type", columnList = "activityType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType activityType;

    private String activityDescription;

    // Related entities
    private Long projectId;
    private String projectName;
    private Long taskId;
    private String taskTitle;

    // Metadata
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    // Timing
    @Column(nullable = false)
    private LocalDateTime activityDate;

    private Integer durationMinutes; // for session-based activities

    @CreationTimestamp
    private LocalDateTime createdAt;
}