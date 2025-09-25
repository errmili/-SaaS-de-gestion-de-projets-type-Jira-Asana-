package com.projectsaas.project.service;

import com.projectsaas.project.dto.UserDto;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AuthIntegrationService authService;

    // Obtenir l'utilisateur actuel
    public UserDto getCurrentUser(String token) {
        UUID tenantId = TenantContext.getTenantId();
        String username = authService.extractUsername(token);

        List<UserDto> users = authService.getTeamMembers(token, tenantId.toString());
        return users.stream()
                .filter(user -> user.getEmail().startsWith(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    // Obtenir un utilisateur par ID
    public UserDto getUserById(UUID userId, String token) {
        UUID tenantId = TenantContext.getTenantId();
        return authService.getUserInfo(userId, token, tenantId.toString());
    }

    // Obtenir l'ID de l'utilisateur actuel
    public UUID getCurrentUserId(String token) {
        String username = authService.extractUsername(token);
        UUID tenantId = TenantContext.getTenantId();

        List<UserDto> users = authService.getTeamMembers(token, tenantId.toString());
        return users.stream()
                .filter(user -> user.getEmail().startsWith(username))
                .map(UserDto::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
