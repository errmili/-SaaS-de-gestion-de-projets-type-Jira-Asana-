// ===========================================
// UserController.java - Gestion des utilisateurs
// ===========================================
package com.projectsaas.auth.controller;

import com.projectsaas.auth.dto.ApiResponse;
import com.projectsaas.auth.dto.UpdateUserRequest;
import com.projectsaas.auth.dto.UserDto;
import com.projectsaas.auth.security.TenantContext;
import com.projectsaas.auth.service.BusinessServices.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        UUID tenantId = TenantContext.getTenantId();
        log.info("Getting all users for tenant: {}", tenantId);

        List<UserDto> users = userService.getUsersByTenant(tenantId);

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID userId) {
        UUID tenantId = TenantContext.getTenantId();
        log.info("Getting user: {} for tenant: {}", userId, tenantId);

        UserDto user = userService.getUserById(userId, tenantId);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        log.info("Updating user: {} for tenant: {}", userId, tenantId);

        UserDto updatedUser = userService.updateUser(userId, tenantId,
                UserDto.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .avatarUrl(request.getAvatarUrl())
                        .build());

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", updatedUser)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        UUID tenantId = TenantContext.getTenantId();
        log.info("Deactivating user: {} for tenant: {}", userId, tenantId);

        userService.deactivateUser(userId, tenantId);

        return ResponseEntity.ok(
                ApiResponse.success("User deactivated successfully", null)
        );
    }
}