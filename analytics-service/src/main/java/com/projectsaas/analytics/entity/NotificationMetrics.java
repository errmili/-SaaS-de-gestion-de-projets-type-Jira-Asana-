package com.projectsaas.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_metrics", indexes = {
        @Index(name = "idx_notification_id", columnList = "notificationId"),
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_sent_date", columnList = "sentAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long notificationId;

    @Column(nullable = false)
    private Long userId;

    private String notificationType; // TASK_ASSIGNED, PROJECT_INVITATION, etc.
    private String deliveryChannel; // EMAIL, WEBSOCKET, PUSH

    // Timing
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;

    // Status
    private String status; // SENT, DELIVERED, READ, FAILED
    private Boolean wasRead;
    private Boolean wasDelivered;

    // Performance metrics
    private Long timeToRead; // minutes from sent to read
    private Long timeToDeliver; // milliseconds from sent to delivered

    // Context
    private Long projectId;
    private Long taskId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}