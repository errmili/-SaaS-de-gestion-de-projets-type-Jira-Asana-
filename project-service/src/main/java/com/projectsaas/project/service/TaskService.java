// ===========================================
// TaskService.java - Service des tâches
// ===========================================
package com.projectsaas.project.service;

import com.projectsaas.project.dto.CreateTaskRequest;
import com.projectsaas.project.dto.TaskDto;
import com.projectsaas.project.dto.UpdateTaskRequest;
import com.projectsaas.project.dto.UserDto;
import com.projectsaas.project.entity.Project;
import com.projectsaas.project.entity.Task;
import com.projectsaas.project.exception.ProjectNotFoundException;
import com.projectsaas.project.exception.TaskNotFoundException;
import com.projectsaas.project.repository.ProjectRepository;
import com.projectsaas.project.repository.TaskRepository;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AuthIntegrationService authService;
    private final NotificationService notificationService;

    // Créer une nouvelle tâche
    public TaskDto createTask(CreateTaskRequest request, String token) {
        UUID tenantId = TenantContext.getTenantId();

        // 1. Vérifier que le projet existe
        Project project = projectRepository.findByIdAndTenantId(request.getProjectId(), tenantId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // 2. Générer la clé de tâche (ex: PROJ-123)
        Integer nextNumber = taskRepository.getNextTaskNumber(tenantId, project.getKey());
        String taskKey = project.getKey() + "-" + nextNumber;

        // 3. Créer la tâche
        Task task = Task.builder()
                .tenantId(tenantId)
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskKey(taskKey)
                .status(Task.TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .taskType(request.getTaskType() != null ? request.getTaskType() : Task.TaskType.TASK)
                .storyPoints(request.getStoryPoints())
                .assigneeId(request.getAssigneeId())
                .reporterId(getCurrentUserId(token))
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);
        log.info("Task created: {} in project: {}", task.getTaskKey(), project.getKey());

        // 4. Notification si tâche assignée
        if (task.getAssigneeId() != null) {
            notificationService.notifyTaskAssigned(task, token);
        }

        return convertToDto(task, token);
    }

    // Obtenir toutes les tâches d'un projet
    public List<TaskDto> getProjectTasks(UUID projectId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Task> tasks = taskRepository.findByProjectIdAndTenantIdOrderByCreatedAtDesc(projectId, tenantId);

        return tasks.stream()
                .map(task -> convertToDto(task, token))
                .collect(Collectors.toList());
    }

    // Obtenir tâches avec pagination
    public Page<TaskDto> getProjectTasksPaginated(UUID projectId, Pageable pageable, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Page<Task> tasks = taskRepository.findByProjectIdAndTenantId(projectId, tenantId, pageable);

        return tasks.map(task -> convertToDto(task, token));
    }

    // Obtenir une tâche par ID
    public TaskDto getTaskById(UUID taskId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        return convertToDto(task, token);
    }

    // Mettre à jour une tâche
    public TaskDto updateTask(UUID taskId, UpdateTaskRequest request, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        // Sauvegarder l'ancien assigné pour notification
        UUID oldAssigneeId = task.getAssigneeId();

        // Mettre à jour les champs
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task = taskRepository.save(task);
        log.info("Task updated: {}", task.getTaskKey());

        // Notifications
        if (!java.util.Objects.equals(oldAssigneeId, task.getAssigneeId())) {
            notificationService.notifyTaskReassigned(task, oldAssigneeId, token);
        }
        notificationService.notifyTaskUpdated(task, token);

        return convertToDto(task, token);
    }

    // Changer le statut d'une tâche
    public TaskDto changeTaskStatus(UUID taskId, Task.TaskStatus newStatus, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Task.TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        task = taskRepository.save(task);
        log.info("Task status changed: {} from {} to {}", task.getTaskKey(), oldStatus, newStatus);

        // Notification
        notificationService.notifyTaskStatusChanged(task, oldStatus, token);

        return convertToDto(task, token);
    }

    // Assigner une tâche
    public TaskDto assignTask(UUID taskId, UUID assigneeId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        UUID oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(assigneeId);

        task = taskRepository.save(task);
        log.info("Task assigned: {} to user: {}", task.getTaskKey(), assigneeId);

        // Notification
        notificationService.notifyTaskAssigned(task, token);
        if (oldAssigneeId != null) {
            notificationService.notifyTaskUnassigned(task, oldAssigneeId, token);
        }

        return convertToDto(task, token);
    }

    // Obtenir les tâches assignées à un utilisateur
    public List<TaskDto> getUserTasks(UUID userId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Task> tasks = taskRepository.findByTenantIdAndAssigneeIdOrderByCreatedAtDesc(tenantId, userId);

        return tasks.stream()
                .map(task -> convertToDto(task, token))
                .collect(Collectors.toList());
    }

    // Obtenir les tâches en retard
    public List<TaskDto> getOverdueTasks(String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Task> tasks = taskRepository.findOverdueTasks(tenantId, LocalDateTime.now());

        return tasks.stream()
                .map(task -> convertToDto(task, token))
                .collect(Collectors.toList());
    }

    // Rechercher des tâches
    public List<TaskDto> searchTasks(String searchTerm, String token) {
        UUID tenantId = TenantContext.getTenantId();

        List<Task> tasks = taskRepository.searchTasks(tenantId, searchTerm);

        return tasks.stream()
                .map(task -> convertToDto(task, token))
                .collect(Collectors.toList());
    }

    // Supprimer une tâche
    public void deleteTask(UUID taskId, String token) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        taskRepository.delete(task);
        log.info("Task deleted: {}", task.getTaskKey());

        // Notification
        notificationService.notifyTaskDeleted(task, token);
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
    private TaskDto convertToDto(Task task, String token) {
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
                .commentCount(task.getComments() != null ? task.getComments().size() : 0)
                .build();
    }
}
