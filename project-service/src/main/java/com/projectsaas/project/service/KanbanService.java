package com.projectsaas.project.service;

import com.projectsaas.project.dto.KanbanBoardDto;
import com.projectsaas.project.dto.TaskDto;
import com.projectsaas.project.entity.Task;
import com.projectsaas.project.entity.Project;
import com.projectsaas.project.exception.ProjectNotFoundException;
import com.projectsaas.project.exception.TaskNotFoundException;
import com.projectsaas.project.repository.ProjectRepository;
import com.projectsaas.project.repository.TaskRepository;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanbanService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;

    // Obtenir le tableau Kanban d'un projet
    public KanbanBoardDto getKanbanBoard(UUID projectId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        // Vérifier que le projet existe
        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Obtenir toutes les tâches du projet
        List<Task> tasks = taskRepository.findByProjectIdAndTenantIdOrderByCreatedAtDesc(projectId, tenantId);

        // Grouper les tâches par statut
        Map<Task.TaskStatus, List<Task>> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus));

        // Créer les colonnes Kanban
        List<KanbanBoardDto.KanbanColumn> columns = Arrays.asList(
                createKanbanColumn("todo", "To Do", Task.TaskStatus.TODO, tasksByStatus, token),
                createKanbanColumn("in-progress", "In Progress", Task.TaskStatus.IN_PROGRESS, tasksByStatus, token),
                createKanbanColumn("in-review", "In Review", Task.TaskStatus.IN_REVIEW, tasksByStatus, token),
                createKanbanColumn("done", "Done", Task.TaskStatus.DONE, tasksByStatus, token)
        );

        // Calculer les statistiques par statut
        Map<String, Integer> statusCounts = tasksByStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().size()
                ));

        return KanbanBoardDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .columns(columns)
                .statusCounts(statusCounts)
                .build();
    }

    // Déplacer une tâche dans le Kanban
    public void moveTask(UUID taskId, String newStatusStr, Integer position, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Task.TaskStatus newStatus = Task.TaskStatus.valueOf(newStatusStr.toUpperCase().replace("-", "_"));
        Task.TaskStatus oldStatus = task.getStatus();

        // Mettre à jour le statut
        task.setStatus(newStatus);
        taskRepository.save(task);

        log.info("Task {} moved from {} to {} at position {}",
                task.getTaskKey(), oldStatus, newStatus, position);

        // Notification temps réel
        notificationService.notifyTaskStatusChanged(task, oldStatus, token);
    }

    // Créer une colonne Kanban
    private KanbanBoardDto.KanbanColumn createKanbanColumn(
            String id,
            String name,
            Task.TaskStatus status,
            Map<Task.TaskStatus, List<Task>> tasksByStatus,
            String token) {

        List<Task> columnTasks = tasksByStatus.getOrDefault(status, Collections.emptyList());

        List<TaskDto> taskDtos = columnTasks.stream()
                .map(task -> convertTaskToDto(task, token))
                .collect(Collectors.toList());

        Integer wipLimit = getWipLimitForStatus(status);

        return KanbanBoardDto.KanbanColumn.builder()
                .id(id)
                .name(name)
                .status(status.name())
                .tasks(taskDtos)
                .taskCount(taskDtos.size())
                .wipLimit(wipLimit)
                .build();
    }

    // Limites WIP par statut
    private Integer getWipLimitForStatus(Task.TaskStatus status) {
        return switch (status) {
            case IN_PROGRESS -> 3;
            case IN_REVIEW -> 2;
            default -> null; // Pas de limite pour TODO et DONE
        };
    }

    // Convertir Task vers TaskDto (simplifié)
    private TaskDto convertTaskToDto(Task task, String token) {
        return TaskDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskKey(task.getTaskKey())
                .status(task.getStatus())
                .priority(task.getPriority())
                .taskType(task.getTaskType())
                .storyPoints(task.getStoryPoints())
                .assigneeId(task.getAssigneeId())
                .reporterId(task.getReporterId())
                .dueDate(task.getDueDate())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}