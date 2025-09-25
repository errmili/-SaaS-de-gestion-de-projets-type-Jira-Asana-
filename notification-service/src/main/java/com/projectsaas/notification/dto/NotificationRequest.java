package com.projectsaas.notification.dto;

import com.projectsaas.notification.enums.DeliveryChannel;
import com.projectsaas.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Delivery channel is required")
    private DeliveryChannel channel;

    private Map<String, String> metadata;
    private Long projectId;
    private Long taskId;
    private String recipientEmail;
    private LocalDateTime scheduledFor;
}