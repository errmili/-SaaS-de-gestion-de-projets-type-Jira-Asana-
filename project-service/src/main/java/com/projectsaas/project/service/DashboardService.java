package com.projectsaas.project.service;

import com.projectsaas.project.dto.DashboardDto;
import com.projectsaas.project.dto.ProjectDto;
import com.projectsaas.project.dto.ProjectStatsDto;
import com.projectsaas.project.dto.TaskDto;
import com.projectsaas.project.entity.Project;
import com.projectsaas.project.entity.Task;
import com.projectsaas.project.repository.ProjectRepository;
import com.projectsaas.project.repository.TaskRepository;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    // Obtenir les données du tableau de bord
    public DashboardDto getDashboardData(String token) {
        UUID tenantId = TenantContext.getTenantId();

        // Projets récents
        List<ProjectDto> recentProjects = projectRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .limit(5)
                .map(project -> convertProjectToDto(project, token))
                .collect(Collectors.toList());

        // Tâches en retard
        List<TaskDto> overdueTasks = taskService.getOverdueTasks(token);

        // Tâches récemment mises à jour
        List<TaskDto> recentlyUpdatedTasks = taskRepository
                .findTop10ByTenantIdOrderByUpdatedAtDesc(tenantId)
                .stream()
                .map(task -> convertTaskToDto(task, token))
                .collect(Collectors.toList());

        // Statistiques
        DashboardDto.DashboardStats stats = calculateDashboardStats(tenantId);

        return DashboardDto.builder()
                .recentProjects(recentProjects)
                .overdueTasks(overdueTasks)
                .recentlyUpdatedTasks(recentlyUpdatedTasks)
                .stats(stats)
                .build();
    }

    // Obtenir les statistiques d'un projet
    public ProjectStatsDto getProjectStats(UUID projectId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Statistiques des tâches par statut
        List<Object[]> statusStats = taskRepository.countTasksByStatus(tenantId, projectId);
        Map<String, Integer> tasksByStatus = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> ((Task.TaskStatus) row[0]).name(),
                        row -> ((Long) row[1]).intValue()
                ));

        // Calculer les totaux
        int totalTasks = tasksByStatus.values().stream().mapToInt(Integer::intValue).sum();
        int completedTasks = tasksByStatus.getOrDefault("DONE", 0);
        int inProgressTasks = tasksByStatus.getOrDefault("IN_PROGRESS", 0);
        int todoTasks = tasksByStatus.getOrDefault("TODO", 0);
        int blockedTasks = tasksByStatus.getOrDefault("BLOCKED", 0);

        // Taux de completion
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        return ProjectStatsDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .blockedTasks(blockedTasks)
                .completionRate(completionRate)
                .build();
    }

    // Calculer les statistiques du dashboard
    private DashboardDto.DashboardStats calculateDashboardStats(UUID tenantId) {
        int totalProjects = (int) projectRepository.count();
        int totalTasks = (int) taskRepository.count();
        int overdueTasksCount = taskRepository.findOverdueTasks(tenantId, LocalDateTime.now()).size();

        return DashboardDto.DashboardStats.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .overdueTasksCount(overdueTasksCount)
                .myOpenTasks(0) // TODO: implémenter
                .activeSprints(0) // TODO: implémenter
                .completedTasksThisWeek(0) // TODO: implémenter
                .build();
    }

    // Méthodes de conversion
    private ProjectDto convertProjectToDto(Project project, String token) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .key(project.getKey())
                .status(project.getStatus())
                .priority(project.getPriority())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private TaskDto convertTaskToDto(Task task, String token) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .taskKey(task.getTaskKey())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assigneeId(task.getAssigneeId())
                .dueDate(task.getDueDate())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}