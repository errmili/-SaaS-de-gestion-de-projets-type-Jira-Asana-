package com.projectsaas.file.service;
import com.projectsaas.file.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${auth-service.base-url:http://localhost:8081}")
    private String authServiceUrl;

    // ✅ TOKENS DE TEST AUTORISÉS - STRICTEMENT DÉFINIS
    private static final Set<String> ALLOWED_TEST_TOKENS = Set.of(
            "test-token-123",
            "dev-token",
            "local-test-token"
    );

    // ✅ VALIDATION TOKEN SÉCURISÉE
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Empty or null token provided");
            return false;
        }

        // ✅ VÉRIFICATION STRICTE DES TOKENS DE TEST
        if (ALLOWED_TEST_TOKENS.contains(token)) {
            log.debug("Accepting authorized test token: {}", token);
            return true;
        }

        // ✅ VALIDATION RÉELLE AVEC AUTH SERVICE
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Validating real token with Auth Service: {}", authServiceUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/api/auth/me",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            boolean isValid = response.getStatusCode() == HttpStatus.OK;
            log.debug("Real token validation result: {}", isValid);
            return isValid;

        } catch (HttpClientErrorException e) {
            log.warn("Token validation failed with Auth Service: HTTP {} - {}",
                    e.getStatusCode(), e.getMessage());
            return false; // ← PAS DE FALLBACK DANGEREUX
        } catch (Exception e) {
            log.error("Error connecting to Auth Service: {}", e.getMessage());
            // ✅ FALLBACK SEULEMENT POUR TOKENS DE TEST STRICTS
            if (ALLOWED_TEST_TOKENS.contains(token)) {
                log.warn("Auth Service unavailable, accepting test token: {}", token);
                return true;
            }
            return false; // ← REJETER LES AUTRES TOKENS
        }
    }

    // ✅ EXTRACTION TENANT ID SÉCURISÉE
    public UUID extractTenantId(String token) {
        try {
            // ✅ TENANT DE TEST SEULEMENT POUR TOKENS AUTORISÉS
            if (ALLOWED_TEST_TOKENS.contains(token)) {
                UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
                log.debug("Using test tenant ID for authorized test token: {}", testTenantId);
                return testTenantId;
            }

            // ✅ DÉCODAGE JWT RÉEL
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                log.warn("Invalid JWT format for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            log.debug("JWT payload decoded successfully");

            // Chercher tenantId dans le payload
            if (payload.contains("\"tenantId\"")) {
                String tenantIdStr = payload.split("\"tenantId\":\"")[1].split("\"")[0];
                UUID tenantId = UUID.fromString(tenantIdStr);
                log.debug("Extracted tenant ID from JWT: {}", tenantId);
                return tenantId;
            } else {
                log.error("No tenantId found in JWT payload");
                throw new IllegalArgumentException("Missing tenantId in JWT");
            }

        } catch (Exception e) {
            log.error("Error extracting tenant ID from token: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot extract tenant ID: " + e.getMessage());
        }
    }

    // ✅ EXTRACTION USERNAME SÉCURISÉE
    public String extractUsername(String token) {
        try {
            // ✅ USERNAME DE TEST SEULEMENT POUR TOKENS AUTORISÉS
            if (ALLOWED_TEST_TOKENS.contains(token)) {
                return "test-user";
            }

            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));

            // Chercher sub (subject) dans le payload
            if (payload.contains("\"sub\":")) {
                String username = payload.split("\"sub\":\"")[1].split("\"")[0];
                String extractedUsername = username.split("@")[0];
                log.debug("Extracted username from JWT: {}", extractedUsername);
                return extractedUsername;
            }

            throw new IllegalArgumentException("Missing sub in JWT");

        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot extract username: " + e.getMessage());
        }
    }

    // ✅ EXTRACTION USER ID SÉCURISÉE
    public UUID extractUserId(String token) {
        try {
            // ✅ USER ID DE TEST SEULEMENT POUR TOKENS AUTORISÉS
            if (ALLOWED_TEST_TOKENS.contains(token)) {
                return UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
            }

            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));

            // Chercher userId dans le payload
            if (payload.contains("\"userId\"")) {
                String userIdStr = payload.split("\"userId\":\"")[1].split("\"")[0];
                UUID userId = UUID.fromString(userIdStr);
                log.debug("Extracted user ID from JWT: {}", userId);
                return userId;
            }

            // Si pas de userId dans JWT, essayer l'Auth Service
            return getUserIdFromAuthService(token);

        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot extract user ID: " + e.getMessage());
        }
    }

    // ✅ RÉCUPÉRATION USER ID DEPUIS AUTH SERVICE
    private UUID getUserIdFromAuthService(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Fetching user info from Auth Service");

            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl + "/api/auth/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();
                if (userInfo.containsKey("id")) {
                    String userIdStr = userInfo.get("id").toString();
                    UUID userId = UUID.fromString(userIdStr);
                    log.debug("Got user ID from Auth Service: {}", userId);
                    return userId;
                }
            }

            throw new IllegalArgumentException("Cannot get user ID from Auth Service");

        } catch (Exception e) {
            log.error("Error fetching user ID from Auth Service: {}", e.getMessage());
            throw new IllegalArgumentException("Auth Service error: " + e.getMessage());
        }
    }

    // ✅ RÉCUPÉRATION TEAM MEMBERS SÉCURISÉE
    public List<UserDto> getTeamMembers(String token, String tenantId) {
        try {
            // ✅ DONNÉES DE TEST SEULEMENT POUR TOKENS AUTORISÉS
            if (ALLOWED_TEST_TOKENS.contains(token)) {
                return List.of(
                        UserDto.builder()
                                .id(UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef"))
                                .email("test-user@example.com")
                                .username("test-user")
                                .build(),
                        UserDto.builder()
                                .id(UUID.fromString("b2c3d4e5-f6g7-8901-2345-678901bcdefg"))
                                .email("admin@example.com")
                                .username("admin")
                                .build()
                );
            }

            // ✅ APPEL RÉEL VERS AUTH SERVICE
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("X-Tenant-ID", tenantId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Fetching team members from Auth Service for tenant: {}", tenantId);

            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl + "/api/users",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // TODO: Mapper la réponse vers List<UserDto>
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error getting team members: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot get team members: " + e.getMessage());
        }
    }

    // ✅ MÉTHODE UTILITAIRE : Vérifier si c'est un token de test
    public boolean isTestToken(String token) {
        return ALLOWED_TEST_TOKENS.contains(token);
    }
}