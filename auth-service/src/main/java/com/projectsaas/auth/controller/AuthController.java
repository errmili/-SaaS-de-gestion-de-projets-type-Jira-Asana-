// ===========================================
// AuthController.java - Contrôleur d'authentification
// ===========================================
package com.projectsaas.auth.controller;

import com.projectsaas.auth.dto.*;
import com.projectsaas.auth.service.BusinessServices.AuthService;
import com.projectsaas.auth.service.JWTService.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {} in tenant: {}", request.getEmail(), request.getTenantSubdomain());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {} in tenant: {}", request.getEmail(), request.getTenantSubdomain());

        UserDto user = authService.register(request);

        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", user)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        LoginResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout attempt");

        // Extraire le token du header "Bearer xxxx"
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        authService.logout(jwtToken);

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", null)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.info("Getting current user info");

        try {
            // 1. Extraire le token du header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7); // Enlever "Bearer "

            // 2. Extraire l'email du token
            String email = jwtService.extractUsername(token);

            log.info("Extracted email from token: {}", email);

            // 3. Récupérer l'utilisateur
            UserDto currentUser = authService.getCurrentUser(email);

            return ResponseEntity.ok(
                    ApiResponse.success("Current user information", currentUser)
            );
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Unable to get current user information: " + e.getMessage()));
        }
    }
}
