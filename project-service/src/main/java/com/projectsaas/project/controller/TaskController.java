package com.projectsaas.project.controller;

import com.projectsaas.project.dto.*;
import com.projectsaas.project.entity.Task;
import com.projectsaas.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDto>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Creating new task: {}", request.getTitle());

        String token = authHeader.substring(7);
        TaskDto task = taskService.createTask(request, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getAllTasks(
            @RequestParam(required = false) UUID projectId,
            Pageable pageable,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting tasks for project: {}", projectId);

        String token = authHeader.substring(7);
        Page<TaskDto> tasks = taskService.getProjectTasksPaginated(projectId, pageable, token);

        return ResponseEntity.ok(
                ApiResponse.success("Tasks retrieved successfully", tasks)
        );
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(
            @PathVariable UUID taskId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting task: {}", taskId);

        String token = authHeader.substring(7);
        TaskDto task = taskService.getTaskById(taskId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Task retrieved successfully", task)
        );
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Updating task: {}", taskId);

        String token = authHeader.substring(7);
        TaskDto task = taskService.updateTask(taskId, request, token);

        return ResponseEntity.ok(
                ApiResponse.success("Task updated successfully", task)
        );
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskDto>> changeTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody ChangeStatusRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Changing task {} status to: {}", taskId, request.getNewStatus());

        String token = authHeader.substring(7);
        TaskDto task = taskService.changeTaskStatus(taskId, request.getNewStatus(), token);

        return ResponseEntity.ok(
                ApiResponse.success("Task status updated successfully", task)
        );
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskDto>> assignTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Assigning task {} to user: {}", taskId, request.getAssigneeId());

        String token = authHeader.substring(7);
        TaskDto task = taskService.assignTask(taskId, request.getAssigneeId(), token);

        return ResponseEntity.ok(
                ApiResponse.success("Task assigned successfully", task)
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID taskId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Deleting task: {}", taskId);

        String token = authHeader.substring(7);
        taskService.deleteTask(taskId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Task deleted successfully", null)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getMyTasks(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting current user tasks");

        String token = authHeader.substring(7);
        // Pour obtenir l'ID utilisateur actuel, on pourrait faire un appel à Auth Service
        // Ici, simplifié pour l'exemple
        List<TaskDto> tasks = taskService.getUserTasks(null, token); // TODO: obtenir user ID

        return ResponseEntity.ok(
                ApiResponse.success("User tasks retrieved successfully", tasks)
        );
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getOverdueTasks(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting overdue tasks");

        String token = authHeader.substring(7);
        List<TaskDto> tasks = taskService.getOverdueTasks(token);

        return ResponseEntity.ok(
                ApiResponse.success("Overdue tasks retrieved successfully", tasks)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TaskDto>>> searchTasks(
            @RequestParam String q,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Searching tasks with term: {}", q);

        String token = authHeader.substring(7);
        List<TaskDto> tasks = taskService.searchTasks(q, token);

        return ResponseEntity.ok(
                ApiResponse.success("Search results", tasks)
        );
    }

    @PutMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateTasks(
            @Valid @RequestBody BulkTaskUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Bulk updating {} tasks", request.getTaskIds().size());

        String token = authHeader.substring(7);
        // TODO: Implémenter le service de mise à jour en masse

        return ResponseEntity.ok(
                ApiResponse.success("Tasks updated successfully", null)
        );
    }
}