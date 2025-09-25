package com.projectsaas.auth.dto;
// ===========================================
// LoginResponse.java - Réponse de connexion
// ===========================================

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn; // en millisecondes
    private UserDto user;
    private String tenant;
}
