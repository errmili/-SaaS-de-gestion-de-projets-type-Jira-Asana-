// ===========================================
// ProjectMemberRepository.java
// ===========================================
package com.projectsaas.project.repository;

import com.projectsaas.project.entity.ProjectMember;
import com.projectsaas.project.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    // Tous les membres d'un projet
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") UUID projectId);

    // Membre spécifique d'un projet
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.userId = :userId")
    Optional<ProjectMember> findByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // Tous les projets d'un utilisateur
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.userId = :userId")
    List<ProjectMember> findByUserId(@Param("userId") UUID userId);

    // Vérifier si utilisateur est membre du projet
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.userId = :userId")
    boolean existsByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // Membres par rôle
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.role = :role")
    List<ProjectMember> findByProjectIdAndRole(@Param("projectId") UUID projectId, @Param("role") ProjectMember.MemberRole role);

    // Compter membres par projet
    @Query("SELECT pm.project.id, COUNT(pm) FROM ProjectMember pm GROUP BY pm.project.id")
    List<Object[]> countMembersByProject();
}