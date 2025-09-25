package com.projectsaas.auth.repository;

import com.projectsaas.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    // Recherche par nom exact
    Optional<Permission> findByName(String name);

    // Permissions par resource
    List<Permission> findByResource(String resource);

    // Permissions par action
    List<Permission> findByAction(String action);

    // Permissions par resource et action
    Optional<Permission> findByResourceAndAction(String resource, String action);

    // Toutes les permissions d'un utilisateur (via ses rôles)
    @Query("SELECT DISTINCT p FROM Permission p " +
            "JOIN p.roles r " +
            "JOIN r.users u " +
            "WHERE u.id = :userId")
    List<Permission> findPermissionsByUserId(@Param("userId") UUID userId);

    // Permissions d'un rôle
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findPermissionsByRoleId(@Param("roleId") UUID roleId);
}