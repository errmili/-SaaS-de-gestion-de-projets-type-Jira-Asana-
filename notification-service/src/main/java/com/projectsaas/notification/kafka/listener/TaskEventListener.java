package com.projectsaas.notification.kafka.listener;

import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.TaskEventDto;
import com.projectsaas.notification.enums.DeliveryChannel;
import com.projectsaas.notification.enums.NotificationType;
import com.projectsaas.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "task.assigned", groupId = "notification-service")
    public void handleTaskAssigned(TaskEventDto taskEvent) {
        log.info("Received task assigned event: {}", taskEvent);

        NotificationRequest request = NotificationRequest.builder()
                .userId(taskEvent.getAssignedUserId())
                .title("Nouvelle tâche assignée: " + taskEvent.getTaskTitle())
                .message(String.format("Vous avez été assigné à la tâche \"%s\" dans le projet %s par %s",
                        taskEvent.getTaskTitle(), taskEvent.getProjectName(), taskEvent.getAssignedByUserName()))
                .type(NotificationType.TASK_ASSIGNED)
                .channel(DeliveryChannel.EMAIL)
                .projectId(taskEvent.getProjectId())
                .taskId(taskEvent.getTaskId())
                .recipientEmail(taskEvent.getAssignedUserEmail())
                .metadata(Map.of(
                        "taskTitle", taskEvent.getTaskTitle(),
                        "projectName", taskEvent.getProjectName(),
                        "assignedBy", taskEvent.getAssignedByUserName(),
                        "userName", taskEvent.getAssignedUserName()
                ))
                .build();

        notificationService.createNotification(request);

        // Également envoyer via WebSocket
        NotificationRequest wsRequest = NotificationRequest.builder()
                .userId(taskEvent.getAssignedUserId())
                .title("Nouvelle tâche assignée")
                .message(taskEvent.getTaskTitle())
                .type(NotificationType.TASK_ASSIGNED)
                .channel(DeliveryChannel.WEBSOCKET)
                .projectId(taskEvent.getProjectId())
                .taskId(taskEvent.getTaskId())
                .metadata(request.getMetadata())
                .build();

        notificationService.createNotification(wsRequest);
    }

    @KafkaListener(topics = "task.updated", groupId = "notification-service")
    public void handleTaskUpdated(TaskEventDto taskEvent) {
        log.info("Received task updated event: {}", taskEvent);

        NotificationRequest request = NotificationRequest.builder()
                .userId(taskEvent.getAssignedUserId())
                .title("Tâche mise à jour: " + taskEvent.getTaskTitle())
                .message(String.format("La tâche \"%s\" a été mise à jour dans le projet %s",
                        taskEvent.getTaskTitle(), taskEvent.getProjectName()))
                .type(NotificationType.TASK_UPDATED)
                .channel(DeliveryChannel.WEBSOCKET)
                .projectId(taskEvent.getProjectId())
                .taskId(taskEvent.getTaskId())
                .metadata(Map.of(
                        "taskTitle", taskEvent.getTaskTitle(),
                        "projectName", taskEvent.getProjectName(),
                        "status", taskEvent.getStatus()
                ))
                .build();

        notificationService.createNotification(request);
    }

    @KafkaListener(topics = "task.deadline.reminder", groupId = "notification-service")
    public void handleDeadlineReminder(TaskEventDto taskEvent) {
        log.info("Received deadline reminder event: {}", taskEvent);

        NotificationRequest request = NotificationRequest.builder()
                .userId(taskEvent.getAssignedUserId())
                .title("Rappel d'échéance: " + taskEvent.getTaskTitle())
                .message(String.format("La tâche \"%s\" arrive à échéance bientôt !", taskEvent.getTaskTitle()))
                .type(NotificationType.DEADLINE_REMINDER)
                .channel(DeliveryChannel.EMAIL)
                .projectId(taskEvent.getProjectId())
                .taskId(taskEvent.getTaskId())
                .recipientEmail(taskEvent.getAssignedUserEmail())
                .metadata(Map.of(
                        "taskTitle", taskEvent.getTaskTitle(),
                        "projectName", taskEvent.getProjectName(),
                        "deadline", taskEvent.getDeadline().toString(),
                        "userName", taskEvent.getAssignedUserName()
                ))
                .build();

        notificationService.createNotification(request);
    }
}