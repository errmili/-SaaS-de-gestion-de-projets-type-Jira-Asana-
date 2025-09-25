// ===========================================
// TaskCommentRepository.java
// ===========================================
package com.projectsaas.project.repository;

import com.projectsaas.project.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    // Commentaire par ID et tenant
    Optional<TaskComment> findByIdAndTenantId(UUID id, UUID tenantId);

    // Tous les commentaires d'une tâche
    List<TaskComment> findByTaskIdAndTenantIdOrderByCreatedAtAsc(UUID taskId, UUID tenantId);

    // Commentaires par auteur
    List<TaskComment> findByTenantIdAndAuthorIdOrderByCreatedAtDesc(UUID tenantId, UUID authorId);

    // Commentaires récents
    List<TaskComment> findTop10ByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    // Compter commentaires par tâche
    @Query("SELECT tc.task.id, COUNT(tc) FROM TaskComment tc " +
            "WHERE tc.tenantId = :tenantId " +
            "GROUP BY tc.task.id")
    List<Object[]> countCommentsByTask(@Param("tenantId") UUID tenantId);
}