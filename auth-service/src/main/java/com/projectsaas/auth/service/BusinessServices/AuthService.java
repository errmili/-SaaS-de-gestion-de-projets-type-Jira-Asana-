// ===========================================
// AuthService.java - Service principal d'authentification
// ===========================================
package com.projectsaas.auth.service.BusinessServices;

import com.projectsaas.auth.dto.LoginRequest;
import com.projectsaas.auth.dto.LoginResponse;
import com.projectsaas.auth.dto.RegisterRequest;
import com.projectsaas.auth.dto.UserDto;
import com.projectsaas.auth.entity.Tenant;
import com.projectsaas.auth.entity.User;
import com.projectsaas.auth.exception.AuthException;
import com.projectsaas.auth.exception.TenantNotFoundException;
import com.projectsaas.auth.repository.TenantRepository;
import com.projectsaas.auth.repository.UserRepository;
import com.projectsaas.auth.security.TenantContext;
import com.projectsaas.auth.service.JWTService.JwtService;
import com.projectsaas.auth.service.JWTService.TenantResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantResolver tenantResolver;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    // private final AuthenticationManager authenticationManager;
    private final UserService userService;

    // Connexion utilisateur
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. Résoudre le tenant
            Tenant tenant = tenantResolver.resolveTenantBySubdomain(request.getTenantSubdomain())
                    .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.getTenantSubdomain()));

            // 2. Vérifier que le tenant est actif
            if (!tenant.getIsActive()) {
                throw new AuthException("Tenant is deactivated");
            }

            // 3. Trouver l'utilisateur
            User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenant.getId())
                    .orElseThrow(() -> new AuthException("Invalid credentials"));

            // 4. Vérifier que l'utilisateur est actif
            if (!user.getIsActive()) {
                throw new AuthException("User account is deactivated");
            }

            // 5. AUTHENTIFICATION MANUELLE - Vérifier le mot de passe directement
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new AuthException("Invalid credentials");
            }

            // 6. Générer les tokens
            String accessToken = jwtService.generateToken(user, tenant.getId());
            String refreshToken = jwtService.generateRefreshToken(user, tenant.getId());

            // 7. Mettre à jour last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 8. Retourner la réponse
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime(accessToken))
                    .user(userService.convertToDto(user))
                    .tenant(tenant.getName())
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user: {} in tenant: {}", request.getEmail(), request.getTenantSubdomain(), e);
            throw new AuthException("Invalid credentials");
        }
    }

    // Inscription utilisateur
    public UserDto register(RegisterRequest request) {
        // 1. Résoudre le tenant
        Tenant tenant = tenantResolver.resolveTenantBySubdomain(request.getTenantSubdomain())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.getTenantSubdomain()));

        // 2. Vérifier les limites du plan
        long currentUserCount = userRepository.countActiveUsersByTenantId(tenant.getId());
        if (currentUserCount >= tenant.getMaxUsers()) {
            throw new AuthException("User limit reached for this plan");
        }

        // 3. Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenant.getId())) {
            throw new AuthException("Email already exists in this organization");
        }

        // 4. Créer l'utilisateur
        User user = User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .emailVerified(false) // À vérifier plus tard
                .build();

        user = userRepository.save(user);

        // 5. Assigner le rôle par défaut (USER)
        userService.assignDefaultRole(user);

        log.info("User registered successfully: {} in tenant: {}", user.getEmail(), tenant.getSubdomain());

        return userService.convertToDto(user);
    }

    // Refresh token
    public LoginResponse refreshToken(String refreshToken) {
        try {
            // 1. Valider que c'est bien un refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new AuthException("Invalid refresh token");
            }

            // 2. Extraire les informations
            String userEmail = jwtService.extractUsername(refreshToken);  // Email complet
            UUID tenantId = jwtService.extractTenantId(refreshToken);

            // 3. Trouver l'utilisateur
            User user = userRepository.findByEmailAndTenantId(userEmail, tenantId)
                    .orElseThrow(() -> new AuthException("User not found"));

            // 4. Valider le refresh token
            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new AuthException("Invalid refresh token");
            }

            // 5. Générer nouveaux tokens
            String newAccessToken = jwtService.generateToken(user, tenantId);
            String newRefreshToken = jwtService.generateRefreshToken(user, tenantId);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime(newAccessToken))
                    .user(userService.convertToDto(user))
                    .tenant(user.getTenant().getName())
                    .build();

        } catch (Exception e) {
            log.error("Refresh token failed", e);
            throw new AuthException("Invalid refresh token");
        }
    }

    public UserDto getCurrentUser(String email) {
        log.info("Getting current user for email: {}", email);

        // Récupérer le tenant ID depuis le contexte
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        log.debug("Looking for user {} in tenant {}", email, tenantId);

        // Trouver l'utilisateur par email et tenant
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Convertir en DTO
        //return userMapper.toDto(user);

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList()))
                .build();
    }

    // Logout (optionnel - côté client principalement)
    public void logout(String token) {
        // En stateless JWT, le logout est principalement côté client
        // Ici on pourrait ajouter le token à une blacklist si nécessaire
        log.info("User logged out");
    }
}