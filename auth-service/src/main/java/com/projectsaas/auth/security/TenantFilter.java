// ===========================================
// TenantFilter.java - Filtre pour résolution tenant CORRIGÉ
// ===========================================
package com.projectsaas.auth.security;

import com.projectsaas.auth.entity.Tenant;
import com.projectsaas.auth.service.JWTService.JwtService;
import com.projectsaas.auth.service.JWTService.TenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Exécuté avant JwtAuthenticationFilter
public class TenantFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            Optional<Tenant> tenant = Optional.empty();

            // 1. D'abord essayer de résoudre depuis l'URL/header (pour les endpoints publics)
            tenant = tenantResolver.resolveTenant(request);

            // 2. Si pas trouvé, essayer d'extraire depuis le JWT (pour les endpoints protégés)
            if (tenant.isEmpty()) {
                tenant = resolveTenantFromJWT(request);
            }

            if (tenant.isPresent()) {
                TenantContext.setTenantId(tenant.get().getId());
                request.setAttribute("tenant", tenant.get());
                log.debug("Tenant resolved: {} (ID: {})", tenant.get().getSubdomain(), tenant.get().getId());
            } else {
                // Pour les endpoints publics, on continue sans tenant
                log.debug("No tenant resolved for request: {}", request.getRequestURI());
            }

            filterChain.doFilter(request, response);

        } finally {
            // Nettoyer le context après la requête
            TenantContext.clear();
        }
    }

    private Optional<Tenant> resolveTenantFromJWT(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Vérifier que le token est valide avant d'extraire les données
                if (jwtService.isValidToken(token)) {
                    UUID tenantId = jwtService.extractTenantId(token);

                    if (tenantId != null) {
                        log.debug("Extracted tenant ID from JWT: {}", tenantId);
                        return tenantResolver.resolveTenantById(tenantId);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve tenant from JWT: {}", e.getMessage());
        }

        return Optional.empty();
    }
}