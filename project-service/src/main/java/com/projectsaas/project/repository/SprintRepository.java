// ===========================================
// SprintRepository.java
// ===========================================
package com.projectsaas.project.repository;

import com.projectsaas.project.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    // Sprint par ID et tenant
    Optional<Sprint> findByIdAndTenantId(UUID id, UUID tenantId);

    // Tous les sprints d'un projet
    List<Sprint> findByProjectIdAndTenantIdOrderByCreatedAtDesc(UUID projectId, UUID tenantId);

    // Sprints par statut
    List<Sprint> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, Sprint.SprintStatus status);

    // Sprint actif d'un projet
    Optional<Sprint> findByProjectIdAndTenantIdAndStatus(UUID projectId, UUID tenantId, Sprint.SprintStatus status);

    // Sprints créés par un utilisateur
    List<Sprint> findByTenantIdAndCreatedByOrderByCreatedAtDesc(UUID tenantId, UUID createdBy);

    // Sprints terminés entre deux dates
    @Query("SELECT s FROM Sprint s " +
            "WHERE s.tenantId = :tenantId " +
            "AND s.status = 'COMPLETED' " +
            "AND s.endDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.endDate DESC")
    List<Sprint> findCompletedSprintsBetweenDates(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Compter sprints par statut
    @Query("SELECT s.status, COUNT(s) FROM Sprint s " +
            "WHERE s.tenantId = :tenantId AND s.project.id = :projectId " +
            "GROUP BY s.status")
    List<Object[]> countSprintsByStatus(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);
}
