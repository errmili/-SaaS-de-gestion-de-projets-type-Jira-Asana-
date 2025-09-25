// ===========================================
// JwtAuthenticationFilter.java - Filtre JWT CORRIGÉ FINAL
// ===========================================
package com.projectsaas.auth.security;

import com.projectsaas.auth.entity.User;
import com.projectsaas.auth.repository.UserRepository;
import com.projectsaas.auth.service.JWTService.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        // Ignorer les endpoints publics
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Vérifier la présence du header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for protected endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token
            final String jwt = authHeader.substring(7);

            // Vérifier que le token est valide
            if (!jwtService.isValidToken(jwt)) {
                log.warn("Invalid JWT token for endpoint: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);
            final UUID tenantId = jwtService.extractTenantId(jwt);

            // Si l'utilisateur n'est pas encore authentifié
            if (userEmail != null && tenantId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                log.debug("Processing JWT authentication for user: {} in tenant: {}", userEmail, tenantId);

                // Chercher l'utilisateur dans la base avec tenant
                Optional<User> userOpt = userRepository.findByEmailAndTenantId(userEmail, tenantId);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Valider le token pour cet utilisateur
                    if (jwtService.isTokenValid(jwt, user)) {

                        // Créer les authorities à partir des rôles
                        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                                .collect(Collectors.toList());

                        // Créer l'authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userEmail,
                                null,
                                authorities
                        );

                        // Définir l'authentication dans le contexte de sécurité
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("User authenticated successfully: {} in tenant: {}", userEmail, tenantId);
                    } else {
                        log.warn("Invalid JWT token for user: {}", userEmail);
                    }
                } else {
                    log.warn("User not found: {} in tenant: {}", userEmail, tenantId);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.equals("/api/auth/login") ||
                requestURI.equals("/api/auth/register") ||
                requestURI.equals("/api/auth/refresh") ||
                requestURI.startsWith("/api/tenants/create") ||
                requestURI.startsWith("/api/tenants/check-subdomain") ||
                requestURI.equals("/api/health") ||
                requestURI.startsWith("/actuator");
    }
}