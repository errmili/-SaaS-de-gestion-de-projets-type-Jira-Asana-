package com.projectsaas.project.controller;

import com.projectsaas.project.dto.ApiResponse;
import com.projectsaas.project.dto.KanbanBoardDto;
import com.projectsaas.project.service.KanbanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/kanban")
@RequiredArgsConstructor
@Slf4j
public class KanbanController {

    private final KanbanService kanbanService;

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<KanbanBoardDto>> getKanbanBoard(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting Kanban board for project: {}", projectId);

        String token = authHeader.substring(7);
        KanbanBoardDto board = kanbanService.getKanbanBoard(projectId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Kanban board retrieved successfully", board)
        );
    }

    @PutMapping("/tasks/{taskId}/move")
    public ResponseEntity<ApiResponse<Void>> moveTask(
            @PathVariable UUID taskId,
            @RequestParam String newStatus,
            @RequestParam(required = false) Integer position,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Moving task {} to status: {}", taskId, newStatus);

        String token = authHeader.substring(7);
        kanbanService.moveTask(taskId, newStatus, position, token);

        return ResponseEntity.ok(
                ApiResponse.success("Task moved successfully", null)
        );
    }
}
