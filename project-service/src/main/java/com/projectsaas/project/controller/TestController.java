package com.projectsaas.project.controller;

import com.projectsaas.project.entity.Project;
import com.projectsaas.project.entity.Task;
import com.projectsaas.project.repository.ProjectRepository;
import com.projectsaas.project.repository.TaskRepository;
import com.projectsaas.project.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestController {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // Test basique - vérifier que l'API fonctionne
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG - Service is running!");
    }

    // Statistiques rapides
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            List<Project> projects = projectRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId);
            final long projectCount = projects.size();
            long taskCountTemp = 0;

            for (Project project : projects) {
                List<Task> tasks = taskRepository.findByProjectIdAndTenantIdOrderByCreatedAtDesc(project.getId(), testTenantId);
                taskCountTemp += tasks.size();
            }

            final long taskCount = taskCountTemp; // Variable finale pour la classe anonyme

            var stats = new Object() {
                public final long projects = projectCount;
                public final long tasks = taskCount;
                public final String tenant = testTenantId.toString();
                public final String status = "✅ All systems operational";
            };

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error in getStats: ", e);
            var errorStats = new Object() {
                public final String error = "Error calculating stats";
                public final String message = e.getMessage();
                public final String status = "❌ Error occurred";
            };
            return ResponseEntity.ok(errorStats);
        }
    }

    // Lister tous les projets - VERSION ULTRA SIMPLE
    @GetMapping("/projects")
    public ResponseEntity<List<SimpleProjectDto>> getAllProjects() {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            List<Project> projects = projectRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId);

            List<SimpleProjectDto> projectDtos = projects.stream()
                    .map(this::convertToSimpleDto)
                    .toList();

            log.info("Found {} projects for tenant: {}", projectDtos.size(), testTenantId);

            return ResponseEntity.ok(projectDtos);
        } catch (Exception e) {
            log.error("Error getting projects: ", e);
            return ResponseEntity.ok(List.of());
        }
    }

    // Test de création rapide d'un projet
    @PostMapping("/projects/quick")
    @Transactional
    public ResponseEntity<SimpleProjectDto> createQuickProject(@RequestBody QuickProjectRequest request) {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
            UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

            Project project = Project.builder()
                    .tenantId(testTenantId)
                    .name(request.getName())
                    .description(request.getDescription())
                    .key(request.getKey().toUpperCase())
                    .status(Project.ProjectStatus.ACTIVE)
                    .priority(Project.Priority.MEDIUM)
                    .createdBy(testUserId)
                    .build();

            project = projectRepository.save(project);

            log.info("Created test project: {} ({})", project.getName(), project.getKey());

            return ResponseEntity.ok(convertToSimpleDto(project));
        } catch (Exception e) {
            log.error("Error creating project: ", e);
            throw new RuntimeException("Failed to create project: " + e.getMessage());
        }

    }

    // Test de création rapide d'une tâche
    @PostMapping("/tasks/quick")
    @Transactional  // ← IMPORTANT : Ajouter cette annotation
    public ResponseEntity<SimpleTaskDto> createQuickTask(@RequestBody QuickTaskRequest request) {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
            UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

            // Vérifier que le projet existe
            Project project = projectRepository.findByIdAndTenantId(request.getProjectId(), testTenantId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Générer la clé de tâche
            Integer nextNumber = taskRepository.getNextTaskNumber(testTenantId, project.getKey());
            String taskKey = project.getKey() + "-" + nextNumber;

            Task task = Task.builder()
                    .tenantId(testTenantId)
                    .project(project)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .taskKey(taskKey)
                    .status(Task.TaskStatus.TODO)
                    .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                    .taskType(request.getTaskType() != null ? request.getTaskType() : Task.TaskType.TASK)
                    .storyPoints(request.getStoryPoints())
                    .reporterId(testUserId)
                    .build();

            task = taskRepository.save(task);

            log.info("Created test task: {} in project: {}", task.getTaskKey(), project.getKey());

            // Convertir vers un DTO simple pour éviter le lazy loading
            return ResponseEntity.ok(convertToSimpleTaskDto(task));

        } catch (Exception e) {
            log.error("Error creating task: ", e);
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    // Test de mise à jour d'une tâche
    @PutMapping("/tasks/{taskId}/status")
    @Transactional  // ← Ajoutez cette annotation
    public ResponseEntity<SimpleTaskDto> updateTaskStatus(@PathVariable UUID taskId,
                                                          @RequestBody UpdateTaskStatusRequest request) {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            Task task = taskRepository.findByIdAndTenantId(taskId, testTenantId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            Task.TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getNewStatus());

            task = taskRepository.save(task);

            log.info("Updated task {} status: {} → {}", task.getTaskKey(), oldStatus, task.getStatus());

            // ✅ CHANGEMENT ICI : Retourner un DTO au lieu de l'entité
            return ResponseEntity.ok(convertToSimpleTaskDto(task));

        } catch (Exception e) {
            log.error("Error updating task status: ", e);
            throw new RuntimeException("Failed to update task status: " + e.getMessage());
        }
    }

    // Test Kanban simple
    @GetMapping("/projects/{projectId}/kanban")
    @Transactional(readOnly = true) // Important pour maintenir la session
    public ResponseEntity<Object> getSimpleKanban(@PathVariable UUID projectId) {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            List<Task> tasks = taskRepository.findByProjectIdAndTenantIdOrderByCreatedAtDesc(projectId, testTenantId);

            // Créer des DTOs simples au lieu de retourner les entités directement
            List<SimpleTaskDto> simpleTasks = tasks.stream()
                    .map(this::convertToSimpleTaskDto)
                    .toList();

            // Grouper par statut avec des DTOs
            var kanban = new Object() {
                public final List<SimpleTaskDto> todo = simpleTasks.stream()
                        .filter(t -> "TODO".equals(t.status))
                        .toList();
                public final List<SimpleTaskDto> inProgress = simpleTasks.stream()
                        .filter(t -> "IN_PROGRESS".equals(t.status))
                        .toList();
                public final List<SimpleTaskDto> inReview = simpleTasks.stream()
                        .filter(t -> "IN_REVIEW".equals(t.status))
                        .toList();
                public final List<SimpleTaskDto> done = simpleTasks.stream()
                        .filter(t -> "DONE".equals(t.status))
                        .toList();
                public final int totalTasks = simpleTasks.size();
                public final String projectIdStr = projectId.toString(); // ← CHANGEMENT ICI
            };

            return ResponseEntity.ok(kanban);
        } catch (Exception e) {
            log.error("Error getting kanban for project {}: ", projectId, e);
            var errorKanban = new Object() {
                public final String error = "Error loading kanban";
                public final String message = e.getMessage();
                public final String projectIdStr = projectId.toString(); // ← ET ICI AUSSI
            };
            return ResponseEntity.ok(errorKanban);
        }
    }



    @GetMapping("/tasks")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SimpleTaskDto>> getAllTasks() {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            List<Task> tasks = taskRepository.findAll().stream()
                    .filter(task -> task.getTenantId().equals(testTenantId))
                    .toList();

            List<SimpleTaskDto> taskDtos = tasks.stream()
                    .map(this::convertToSimpleTaskDto)
                    .toList();

            log.info("Found {} tasks for tenant: {}", taskDtos.size(), testTenantId);

            return ResponseEntity.ok(taskDtos);
        } catch (Exception e) {
            log.error("Error getting tasks: ", e);
            return ResponseEntity.ok(List.of());
        }
    }

    // Obtenir les tâches d'un projet spécifique
    @GetMapping("/projects/{projectId}/tasks")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SimpleTaskDto>> getProjectTasks(@PathVariable UUID projectId) {
        try {
            UUID testTenantId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

            List<Task> tasks = taskRepository.findByProjectIdAndTenantIdOrderByCreatedAtDesc(projectId, testTenantId);

            List<SimpleTaskDto> taskDtos = tasks.stream()
                    .map(this::convertToSimpleTaskDto)
                    .toList();

            log.info("Found {} tasks for project: {}", taskDtos.size(), projectId);

            return ResponseEntity.ok(taskDtos);
        } catch (Exception e) {
            log.error("Error getting project tasks: ", e);
            return ResponseEntity.ok(List.of());
        }
    }



    // DTO simple
    public static class SimpleProjectDto {
        public UUID id;
        public String name;
        public String description;
        public String key;
        public String status;
        public String priority;
        public String createdAt;
    }

    public static class QuickProjectRequest {
        private String name;
        private String description;
        private String key;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }

    // Méthode de conversion
    private SimpleProjectDto convertToSimpleDto(Project project) {
        SimpleProjectDto dto = new SimpleProjectDto();
        dto.id = project.getId();
        dto.name = project.getName();
        dto.description = project.getDescription();
        dto.key = project.getKey();
        dto.status = project.getStatus().name();
        dto.priority = project.getPriority().name();
        dto.createdAt = project.getCreatedAt() != null ? project.getCreatedAt().toString() : null;
        return dto;
    }


    public static class QuickTaskRequest {
        private UUID projectId;
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.TaskType taskType;
        private Integer storyPoints;

        // Getters et Setters
        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Task.Priority getPriority() { return priority; }
        public void setPriority(Task.Priority priority) { this.priority = priority; }

        public Task.TaskType getTaskType() { return taskType; }
        public void setTaskType(Task.TaskType taskType) { this.taskType = taskType; }

        public Integer getStoryPoints() { return storyPoints; }
        public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }
    }

    public static class UpdateTaskStatusRequest {
        private Task.TaskStatus newStatus;

        public Task.TaskStatus getNewStatus() { return newStatus; }
        public void setNewStatus(Task.TaskStatus newStatus) { this.newStatus = newStatus; }
    }

    public static class SimpleTaskDto {
        public UUID id;
        public String title;
        public String description;
        public String taskKey;
        public String status;
        public String priority;
        public String taskType;
        public Integer storyPoints;
        public UUID assigneeId;
        public UUID reporterId;
        public String createdAt;
    }

    // Méthode de conversion pour éviter le lazy loading
    private SimpleTaskDto convertToSimpleTaskDto(Task task) {
        SimpleTaskDto dto = new SimpleTaskDto();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.description = task.getDescription();
        dto.taskKey = task.getTaskKey();
        dto.status = task.getStatus().name();
        dto.priority = task.getPriority().name();
        dto.taskType = task.getTaskType().name();
        dto.storyPoints = task.getStoryPoints();
        dto.assigneeId = task.getAssigneeId();
        dto.reporterId = task.getReporterId();
        dto.createdAt = task.getCreatedAt() != null ? task.getCreatedAt().toString() : null;
        return dto;
    }

}