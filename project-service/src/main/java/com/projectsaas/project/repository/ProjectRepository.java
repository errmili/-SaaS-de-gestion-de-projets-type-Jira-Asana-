// ===========================================
// ProjectRepository.java
// ===========================================
package com.projectsaas.project.repository;

import com.projectsaas.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Tous les projets d'un tenant
    List<Project> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    // Projet par ID et tenant (sécurité)
    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);

    // Projet par clé et tenant
    Optional<Project> findByKeyAndTenantId(String key, UUID tenantId);

    // Vérifier si clé existe dans tenant
    boolean existsByKeyAndTenantId(String key, UUID tenantId);

    // Projets par statut
    List<Project> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, Project.ProjectStatus status);

    // Projets où l'utilisateur est créateur
    List<Project> findByTenantIdAndCreatedByOrderByCreatedAtDesc(UUID tenantId, UUID createdBy);

    // Projets où l'utilisateur est membre
    @Query("SELECT DISTINCT p FROM Project p " +
            "JOIN p.members m " +
            "WHERE p.tenantId = :tenantId AND m.userId = :userId " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsByMember(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);

    // Recherche par nom ou description
    @Query("SELECT p FROM Project p " +
            "WHERE p.tenantId = :tenantId " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Project> searchProjects(@Param("tenantId") UUID tenantId, @Param("search") String search);

    // Compter projets par statut
    @Query("SELECT p.status, COUNT(p) FROM Project p " +
            "WHERE p.tenantId = :tenantId " +
            "GROUP BY p.status")
    List<Object[]> countProjectsByStatus(@Param("tenantId") UUID tenantId);
}