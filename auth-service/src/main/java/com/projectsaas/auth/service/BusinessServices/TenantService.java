// ===========================================
// TenantService.java - Service de gestion des tenants
// ===========================================
package com.projectsaas.auth.service.BusinessServices;

import com.projectsaas.auth.dto.CreateTenantRequest;
import com.projectsaas.auth.dto.TenantDto;
import com.projectsaas.auth.entity.Role;
import com.projectsaas.auth.entity.Tenant;
import com.projectsaas.auth.entity.User;
import com.projectsaas.auth.exception.TenantAlreadyExistsException;
import com.projectsaas.auth.exception.TenantNotFoundException;
import com.projectsaas.auth.repository.RoleRepository;
import com.projectsaas.auth.repository.TenantRepository;
import com.projectsaas.auth.repository.UserRepository;
import com.projectsaas.auth.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Créer une nouvelle organisation avec admin
    public TenantDto createTenant(CreateTenantRequest request) {
        // 1. Vérifier que le subdomain n'existe pas
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new TenantAlreadyExistsException("Subdomain already exists: " + request.getSubdomain());
        }

        // 2. Vérifier que le nom n'existe pas
        if (tenantRepository.existsByName(request.getName())) {
            throw new TenantAlreadyExistsException("Organization name already exists: " + request.getName());
        }

        // 3. Créer le tenant
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .subdomain(request.getSubdomain().toLowerCase())
                .planType(Tenant.PlanType.FREE)
                .maxUsers(5) // Plan gratuit = 5 utilisateurs max
                .isActive(true)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created: {} with subdomain: {}", tenant.getName(), tenant.getSubdomain());

        // 4. Créer les rôles par défaut
        createDefaultRoles(tenant);

        // 5. Créer l'utilisateur admin
        createAdminUser(tenant, request);

        return convertToDto(tenant);
    }

    // Créer les rôles par défaut pour un tenant
    private void createDefaultRoles(Tenant tenant) {
        // Rôle ADMIN
        Role adminRole = Role.builder()
                .tenant(tenant)
                .name("ADMIN")
                .description("Administrator with full access")
                .isSystem(true)
                .build();
        roleRepository.save(adminRole);

        // Rôle MANAGER
        Role managerRole = Role.builder()
                .tenant(tenant)
                .name("MANAGER")
                .description("Project manager with team access")
                .isSystem(true)
                .build();
        roleRepository.save(managerRole);

        // Rôle USER
        Role userRole = Role.builder()
                .tenant(tenant)
                .name("USER")
                .description("Standard user with basic access")
                .isSystem(true)
                .build();
        roleRepository.save(userRole);

        log.info("Default roles created for tenant: {}", tenant.getSubdomain());
    }

    // Créer l'utilisateur admin lors de la création du tenant
    private void createAdminUser(Tenant tenant, CreateTenantRequest request) {
        // Créer l'utilisateur admin
        User adminUser = User.builder()
                .tenant(tenant)
                .email(request.getAdminEmail())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .isActive(true)
                .emailVerified(true) // Admin automatiquement vérifié
                .build();

        adminUser = userRepository.save(adminUser);

        // Assigner le rôle ADMIN
        Role adminRole = roleRepository.findByNameAndTenantId("ADMIN", tenant.getId())
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        adminUser.getRoles().add(adminRole);
        userRepository.save(adminUser);

        log.info("Admin user created: {} for tenant: {}", adminUser.getEmail(), tenant.getSubdomain());
    }

    // Obtenir le tenant actuel depuis le context
    public TenantDto getCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantNotFoundException("No tenant context found");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        return convertToDto(tenant);
    }

    // Vérifier si un subdomain est disponible
    public boolean isSubdomainAvailable(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return false;
        }

        // Nettoyer le subdomain
        String cleanSubdomain = subdomain.toLowerCase().trim();

        // Vérifier format (lettres, chiffres, tirets seulement)
        if (!cleanSubdomain.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$")) {
            return false;
        }

        // Vérifier longueur
        if (cleanSubdomain.length() < 2 || cleanSubdomain.length() > 100) {
            return false;
        }

        // Vérifier disponibilité
        return !tenantRepository.existsBySubdomain(cleanSubdomain);
    }

    // Obtenir tenant par subdomain
    public TenantDto getTenantBySubdomain(String subdomain) {
        Tenant tenant = tenantRepository.findBySubdomainAndIsActiveTrue(subdomain)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + subdomain));

        return convertToDto(tenant);
    }

    // Mettre à jour un tenant
    public TenantDto updateTenant(UUID tenantId, TenantDto updateDto) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        // Mettre à jour les champs autorisés
        if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
            tenant.setName(updateDto.getName().trim());
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant updated: {}", tenant.getSubdomain());

        return convertToDto(tenant);
    }

    // Désactiver un tenant
    public void deactivateTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        tenant.setIsActive(false);
        tenantRepository.save(tenant);

        log.info("Tenant deactivated: {}", tenant.getSubdomain());
    }

    // Convertir entité vers DTO
    public TenantDto convertToDto(Tenant tenant) {
        // Compter les utilisateurs actifs
        long currentUsers = userRepository.countActiveUsersByTenantId(tenant.getId());

        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .planType(tenant.getPlanType())
                .maxUsers(tenant.getMaxUsers())
                .currentUsers((int) currentUsers)
                .isActive(tenant.getIsActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}