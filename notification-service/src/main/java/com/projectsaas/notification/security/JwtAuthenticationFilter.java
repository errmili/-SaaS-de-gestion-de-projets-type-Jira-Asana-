// ===========================================
// JwtAuthenticationFilter.java - Filtre JWT pour Notification Service
// ===========================================
package com.projectsaas.notification.security;

import com.projectsaas.notification.service.AuthIntegrationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthIntegrationService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        try {
            String bearer = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);

                if (authService.validateToken(token)) {
                    UsernamePasswordAuthenticationToken auth =
                            buildAuthentication(token, req);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();   // ← libère le ThreadLocal
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String token,
                                                                    HttpServletRequest req) {
        String username  = authService.extractUsername(token);
        UUID tenantId    = authService.extractTenantId(token);

        TenantContext.setTenantId(tenantId);
        req.setAttribute("tenantId", tenantId);
        req.setAttribute("username", username);
        req.setAttribute("token", token);

        return new UsernamePasswordAuthenticationToken(
                username, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}