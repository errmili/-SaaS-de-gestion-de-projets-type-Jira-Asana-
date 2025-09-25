package com.projectsaas.project.controller;

import com.projectsaas.project.dto.*;
import com.projectsaas.project.entity.ProjectMember;
import com.projectsaas.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Creating new project: {}", request.getName());

        String token = authHeader.substring(7); // Remove "Bearer "
        ProjectDto project = projectService.createProject(request, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllProjects(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting all projects");

        String token = authHeader.substring(7);
        List<ProjectDto> projects = projectService.getAllProjects(token);

        return ResponseEntity.ok(
                ApiResponse.success("Projects retrieved successfully", projects)
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting project: {}", projectId);

        String token = authHeader.substring(7);
        ProjectDto project = projectService.getProjectById(projectId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Project retrieved successfully", project)
        );
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Updating project: {}", projectId);

        String token = authHeader.substring(7);
        ProjectDto project = projectService.updateProject(projectId, request, token);

        return ResponseEntity.ok(
                ApiResponse.success("Project updated successfully", project)
        );
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Deleting project: {}", projectId);

        String token = authHeader.substring(7);
        projectService.deleteProject(projectId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Project deleted successfully", null)
        );
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<Void>> addProjectMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Adding member to project {}: {}", projectId, request.getUserId());

        String token = authHeader.substring(7);
        projectService.addProjectMember(projectId, request.getUserId(), request.getRole(), token);

        return ResponseEntity.ok(
                ApiResponse.success("Member added successfully", null)
        );
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Removing member from project {}: {}", projectId, userId);

        String token = authHeader.substring(7);
        projectService.removeProjectMember(projectId, userId, token);

        return ResponseEntity.ok(
                ApiResponse.success("Member removed successfully", null)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getUserProjects(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Getting user projects");

        String token = authHeader.substring(7);
        List<ProjectDto> projects = projectService.getUserProjects(token);

        return ResponseEntity.ok(
                ApiResponse.success("User projects retrieved successfully", projects)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> searchProjects(
            @RequestParam String q,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Searching projects with term: {}", q);

        String token = authHeader.substring(7);
        List<ProjectDto> projects = projectService.searchProjects(q, token);

        return ResponseEntity.ok(
                ApiResponse.success("Search results", projects)
        );
    }
}
