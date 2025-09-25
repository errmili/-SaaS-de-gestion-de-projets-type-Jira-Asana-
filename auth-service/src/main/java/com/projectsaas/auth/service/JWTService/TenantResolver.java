package com.projectsaas.auth.service.JWTService;

import com.projectsaas.auth.entity.Tenant;
import com.projectsaas.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantResolver {

    private final TenantRepository tenantRepository;

    // Résoudre tenant depuis la requête HTTP
    public Optional<Tenant> resolveTenant(HttpServletRequest request) {
        // Méthode 1: Header X-Tenant-ID
        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null && !tenantHeader.isEmpty()) {
            return tenantRepository.findBySubdomainIgnoreCase(tenantHeader);
        }

        // Méthode 2: Subdomain (ex: company1.localhost:8081)
        String serverName = request.getServerName();
        String subdomain = extractSubdomain(serverName);

        if (subdomain != null && !subdomain.isEmpty()) {
            return tenantRepository.findBySubdomainIgnoreCase(subdomain);
        }

        log.debug("No tenant found for request: {}", request.getRequestURL());
        return Optional.empty();
    }

    // Extraire subdomain depuis hostname
    private String extractSubdomain(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return null;
        }

        // Pour localhost en développement
        if (hostname.equals("localhost") || hostname.startsWith("127.0.0.1")) {
            return "default"; // Tenant par défaut en dev
        }

        // Pour les vrais domaines: company1.myapp.com -> company1
        String[] parts = hostname.split("\\.");
        if (parts.length >= 3) {
            return parts[0]; // Premier segment = subdomain
        }

        return null;
    }

    // Résoudre tenant par subdomain directement
    public Optional<Tenant> resolveTenantBySubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return Optional.empty();
        }

        return tenantRepository.findBySubdomainIgnoreCase(subdomain.trim());
    }

    // NOUVELLE MÉTHODE - Résoudre tenant par ID
    public Optional<Tenant> resolveTenantById(UUID tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }

        try {
            return tenantRepository.findById(tenantId);
        } catch (Exception e) {
            log.error("Error resolving tenant by ID {}: {}", tenantId, e.getMessage());
            return Optional.empty();
        }
    }
}