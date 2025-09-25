// ===========================================
// ProjectService.java - CORRIGÉ
// ===========================================
package com.projectsaas.project.service;

import com.projectsaas.project.dto.CreateProjectRequest;
import com.projectsaas.project.dto.ProjectDto;
import com.projectsaas.project.dto.UpdateProjectRequest;
import com.projectsaas.project.dto.UserDto;
import com.projectsaas.project.entity.Project;
import com.projectsaas.project.entity.ProjectMember;
import com.projectsaas.project.exception.ProjectAlreadyExistsException;
import com.projectsaas.project.exception.ProjectNotFoundException;
import com.projectsaas.project.repository.ProjectRepository;
import com.projectsaas.project.repository.ProjectMemberRepository;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthIntegrationService authService;
    private final NotificationService notificationService;

    // Créer un nouveau projet
    public ProjectDto createProject(CreateProjectRequest request, String token) {
        UUID tenantId = TenantContext.getTenantId();

        // 1. Vérifier que la clé n'existe pas déjà
        if (projectRepository.existsByKeyAndTenantId(request.getKey(), tenantId)) {
            throw new ProjectAlreadyExistsException("Project key already exists: " + request.getKey());
        }

        // 2. Créer le projet
        Project project = Project.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .key(request.getKey().toUpperCase())
                .status(Project.ProjectStatus.ACTIVE)
                .priority(request.getPriority() != null ? request.getPriority() : Project.Priority.MEDIUM)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(getCurrentUserId(token))
                .build();

        project = projectRepository.save(project);
        log.info("Project created: {} ({}) in tenant: {}", project.getName(), project.getKey(), tenantId);

        // 3. Ajouter le créateur comme owner du projet
        addProjectOwner(project, getCurrentUserId(token));

        // 4. Notification
        notificationService.notifyProjectCreated(project, token);

        return convertToDto(project, token);
    }

    // Mettre à jour un projet - MÉTHODE CORRIGÉE
    public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Mettre à jour les champs - CORRIGÉ : utiliser 'request' au lieu de 'updateDto'
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }

        project = projectRepository.save(project);
        log.info("Project updated: {} in tenant: {}", project.getName(), tenantId);

        // Notification
        notificationService.notifyProjectUpdated(project, token);

        return convertToDto(project, token);
    }

    // Obtenir tous les projets du tenant
    public List<ProjectDto> getAllProjects(String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Project> projects = projectRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        return projects.stream()
                .map(project -> convertToDto(project, token))
                .collect(Collectors.toList());
    }

    // Obtenir un projet par ID
    public ProjectDto getProjectById(UUID projectId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        return convertToDto(project, token);
    }

    // Supprimer un projet
    public void deleteProject(UUID projectId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        projectRepository.delete(project);
        log.info("Project deleted: {} in tenant: {}", project.getName(), tenantId);

        // Notification
        notificationService.notifyProjectDeleted(project, token);
    }

    // Ajouter un membre au projet
    public void addProjectMember(UUID projectId, UUID userId, ProjectMember.MemberRole role, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Vérifier si l'utilisateur n'est pas déjà membre
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .userId(userId)
                .role(role)
                .build();

        projectMemberRepository.save(member);
        log.info("Member added to project {}: {}", project.getKey(), userId);

        // Notification
        notificationService.notifyMemberAdded(project, userId, token);
    }

    // Retirer un membre du projet
    public void removeProjectMember(UUID projectId, UUID userId, String token) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        projectMemberRepository.delete(member);
        log.info("Member removed from project {}: {}", projectId, userId);
    }

    // Obtenir les projets d'un utilisateur
    public List<ProjectDto> getUserProjects(String token) {
        UUID tenantId = TenantContext.getTenantId();
        UUID userId = getCurrentUserId(token);

        List<Project> projects = projectRepository.findProjectsByMember(tenantId, userId);

        return projects.stream()
                .map(project -> convertToDto(project, token))
                .collect(Collectors.toList());
    }

    // Rechercher des projets
    public List<ProjectDto> searchProjects(String searchTerm, String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Project> projects = projectRepository.searchProjects(tenantId, searchTerm);

        return projects.stream()
                .map(project -> convertToDto(project, token))
                .collect(Collectors.toList());
    }

    // Ajouter le créateur comme owner
    private void addProjectOwner(Project project, UUID userId) {
        ProjectMember owner = ProjectMember.builder()
                .project(project)
                .userId(userId)
                .role(ProjectMember.MemberRole.OWNER)
                .build();

        projectMemberRepository.save(owner);
    }

    // Obtenir l'ID de l'utilisateur actuel
    private UUID getCurrentUserId(String token) {
        String username = authService.extractUsername(token);
        UUID tenantId = TenantContext.getTenantId();

        List<UserDto> users = authService.getTeamMembers(token, tenantId.toString());
        return users.stream()
                .filter(user -> user.getEmail().startsWith(username))
                .map(UserDto::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    // Convertir entité vers DTO
    private ProjectDto convertToDto(Project project, String token) {
        // Obtenir les membres du projet
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .key(project.getKey())
                .status(project.getStatus())
                .priority(project.getPriority())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdBy(project.getCreatedBy())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .memberCount(members.size())
                .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .build();
    }
}