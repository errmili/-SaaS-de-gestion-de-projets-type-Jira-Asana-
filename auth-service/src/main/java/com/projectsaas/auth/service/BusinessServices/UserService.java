// ===========================================
// UserService.java - Gestion des utilisateurs
// ===========================================
package com.projectsaas.auth.service.BusinessServices;

import com.projectsaas.auth.dto.UserDto;
import com.projectsaas.auth.entity.Role;
import com.projectsaas.auth.entity.User;
import com.projectsaas.auth.exception.UserNotFoundException;
import com.projectsaas.auth.repository.RoleRepository;
import com.projectsaas.auth.repository.TenantRepository;
import com.projectsaas.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    // Obtenir tous les utilisateurs d'un tenant
    public List<UserDto> getUsersByTenant(UUID tenantId) {
        return userRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Obtenir un utilisateur par ID (avec validation tenant)
    public UserDto getUserById(UUID userId, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return convertToDto(user);
    }

    // Mettre à jour un utilisateur
    public UserDto updateUser(UUID userId, UUID tenantId, UserDto updateDto) {
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Mettre à jour les champs autorisés
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDto.getAvatarUrl());
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getEmail());

        return convertToDto(user);
    }

    // Désactiver un utilisateur
    public void deactivateUser(UUID userId, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated: {}", user.getEmail());
    }

    // Assigner le rôle par défaut
    public void assignDefaultRole(User user) {
        Role defaultRole = roleRepository.findByNameAndTenantId("USER", user.getTenant().getId())
                .orElseGet(() -> createDefaultUserRole(user.getTenant().getId()));

        user.getRoles().add(defaultRole);
        userRepository.save(user);
    }

    // Créer le rôle USER par défaut
    private Role createDefaultUserRole(UUID tenantId) {
        Role userRole = Role.builder()
                .tenant(tenantRepository.getReferenceById(tenantId))
                .name("USER")
                .description("Default user role")
                .isSystem(true)
                .build();

        return roleRepository.save(userRole);
    }

    // Convertir entité vers DTO
    public UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}