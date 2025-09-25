// ===========================================
// NotificationService.java - CLASSE SIMPLIFIÉE
// ===========================================
package com.projectsaas.project.service;

import com.projectsaas.project.entity.Project;
import com.projectsaas.project.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    // Pour l'instant, juste des logs. Plus tard: WebSockets

    public void notifyProjectCreated(Project project, String token) {
        log.info("📢 Project created: {} by user", project.getName());
    }

    public void notifyProjectUpdated(Project project, String token) {
        log.info("📢 Project updated: {}", project.getName());
    }

    public void notifyProjectDeleted(Project project, String token) {
        log.info("📢 Project deleted: {}", project.getName());
    }

    public void notifyMemberAdded(Project project, UUID userId, String token) {
        log.info("📢 Member {} added to project {}", userId, project.getKey());
    }

    public void notifyTaskAssigned(Task task, String token) {
        log.info("📢 Task {} assigned to {}", task.getTaskKey(), task.getAssigneeId());
    }

    public void notifyTaskUpdated(Task task, String token) {
        log.info("📢 Task {} updated", task.getTaskKey());
    }

    public void notifyTaskStatusChanged(Task task, Task.TaskStatus oldStatus, String token) {
        log.info("📢 Task {} status: {} → {}", task.getTaskKey(), oldStatus, task.getStatus());
    }

    public void notifyTaskReassigned(Task task, UUID oldAssigneeId, String token) {
        log.info("📢 Task {} reassigned: {} → {}", task.getTaskKey(), oldAssigneeId, task.getAssigneeId());
    }

    public void notifyTaskUnassigned(Task task, UUID oldAssigneeId, String token) {
        log.info("📢 Task {} unassigned from {}", task.getTaskKey(), oldAssigneeId);
    }

    public void notifyTaskDeleted(Task task, String token) {
        log.info("📢 Task {} deleted", task.getTaskKey());
    }
}