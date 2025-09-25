package com.projectsaas.notification.dto;

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
public class UserEventDto {
    private Long userId;
    private String userName;
    private String userEmail;
    private Long invitedByUserId;
    private String invitedByUserName;
    private Long projectId;
    private String projectName;
    private String eventType; // INVITED, REGISTERED, PROFILE_UPDATED
    private LocalDateTime timestamp;
    private Map<String, String> additionalData;
}