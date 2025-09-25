package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    private String alertType; // WARNING, ERROR, INFO
    private String title;
    private String message;
    private String severity; // HIGH, MEDIUM, LOW
    private LocalDateTime createdAt;
    private String actionUrl;
    private Boolean isRead;
}