package com.projectsaas.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {

    private Long userId;
    private String userName;
    private String userEmail;
    private String eventType; // PROFILE_UPDATED, PASSWORD_CHANGED, etc.
}