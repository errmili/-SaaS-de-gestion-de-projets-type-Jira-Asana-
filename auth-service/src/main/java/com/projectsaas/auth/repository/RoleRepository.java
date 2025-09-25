package com.projectsaas.auth.repository;

import com.projectsaas.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Tous les rôles d'un tenant
    List<Role> findByTenantId(UUID tenantId);

    // Recherche par nom et tenant
    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);

    // Rôles système (définis par défaut)
    @Query("SELECT r FROM Role r WHERE r.isSystem = true AND r.tenant.id = :tenantId")
    List<Role> findSystemRolesByTenantId(@Param("tenantId") UUID tenantId);

    // Vérifier si rôle existe dans tenant
    boolean existsByNameAndTenantId(String name, UUID tenantId);

    // Recherche par ID avec validation tenant
    @Query("SELECT r FROM Role r WHERE r.id = :id AND r.tenant.id = :tenantId")
    Optional<Role> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}