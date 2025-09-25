package com.projectsaas.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Builder.Default
    private Boolean emailNotifications = true;

    @Builder.Default
    private Boolean pushNotifications = true;

    @Builder.Default
    private Boolean websocketNotifications = true;

    @Builder.Default
    private Boolean taskAssigned = true;

    @Builder.Default
    private Boolean taskUpdated = true;

    @Builder.Default
    private Boolean projectInvitation = true;

    @Builder.Default
    private Boolean deadlineReminder = true;

    @Builder.Default
    private Boolean commentMentions = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}