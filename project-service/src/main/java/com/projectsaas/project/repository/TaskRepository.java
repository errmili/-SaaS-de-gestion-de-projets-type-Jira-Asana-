// ===========================================
// TaskRepository.java
// ===========================================
package com.projectsaas.project.repository;

import com.projectsaas.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Tâche par ID et tenant (sécurité)
    Optional<Task> findByIdAndTenantId(UUID id, UUID tenantId);

    // Tâche par clé et tenant
    Optional<Task> findByTaskKeyAndTenantId(String taskKey, UUID tenantId);

    // Vérifier si clé tâche existe
    boolean existsByTaskKeyAndTenantId(String taskKey, UUID tenantId);

    // Toutes les tâches d'un projet
    List<Task> findByProjectIdAndTenantIdOrderByCreatedAtDesc(UUID projectId, UUID tenantId);

    // Tâches d'un projet avec pagination
    Page<Task> findByProjectIdAndTenantId(UUID projectId, UUID tenantId, Pageable pageable);

    // Tâches assignées à un utilisateur
    List<Task> findByTenantIdAndAssigneeIdOrderByCreatedAtDesc(UUID tenantId, UUID assigneeId);

    // Tâches créées par un utilisateur
    List<Task> findByTenantIdAndReporterIdOrderByCreatedAtDesc(UUID tenantId, UUID reporterId);

    // Tâches par statut
    List<Task> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, Task.TaskStatus status);

    // Tâches d'un sprint
    List<Task> findBySprintIdAndTenantIdOrderByCreatedAtDesc(UUID sprintId, UUID tenantId);

    // Tâches sans sprint (backlog)
    List<Task> findByProjectIdAndTenantIdAndSprintIsNullOrderByCreatedAtDesc(UUID projectId, UUID tenantId);

    // Tâches en retard
    @Query("SELECT t FROM Task t " +
            "WHERE t.tenantId = :tenantId " +
            "AND t.dueDate < :now " +
            "AND t.status NOT IN ('DONE') " +
            "ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    // Recherche de tâches
    @Query("SELECT t FROM Task t " +
            "WHERE t.tenantId = :tenantId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.taskKey) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY t.createdAt DESC")
    List<Task> searchTasks(@Param("tenantId") UUID tenantId, @Param("search") String search);

    // Statistiques par statut
    @Query("SELECT t.status, COUNT(t) FROM Task t " +
            "WHERE t.tenantId = :tenantId AND t.project.id = :projectId " +
            "GROUP BY t.status")
    List<Object[]> countTasksByStatus(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    // Tâches récemment mises à jour
    List<Task> findTop10ByTenantIdOrderByUpdatedAtDesc(UUID tenantId);

    // Prochain numéro de tâche pour un projet
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.taskKey, LENGTH(:projectKey) + 2) AS integer)), 0) + 1 " +
            "FROM Task t " +
            "WHERE t.tenantId = :tenantId " +
            "AND t.project.key = :projectKey " +
            "AND t.taskKey LIKE CONCAT(:projectKey, '-%')")
    Integer getNextTaskNumber(@Param("tenantId") UUID tenantId, @Param("projectKey") String projectKey);
}