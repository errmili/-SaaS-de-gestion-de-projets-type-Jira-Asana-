package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProductivityDto {
    private Long userId;
    private String userName;

    // Task metrics
    private Integer tasksCompleted;
    private Integer tasksAssigned;
    private Integer tasksOverdue;

    // Time metrics
    private Double averageTaskDuration; // hours
    private Double averageTimeToStart; // hours
    private Integer totalActiveHours;

    // Performance scores
    private Double productivityScore; // 0-100
    private Double onTimeDeliveryRate; // percentage
    private Double collaborationScore; // based on team interactions

    // Activity
    private LocalDateTime lastActivity;
    private Integer projectsInvolved;

    // Trends
    private Double productivityTrend;
    private List<DailyActivityDto> dailyActivity;
}