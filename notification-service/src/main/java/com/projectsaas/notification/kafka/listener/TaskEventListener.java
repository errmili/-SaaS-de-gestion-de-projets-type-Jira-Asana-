package com.projectsaas.notification.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.TaskEventDto;
import com.projectsaas.notification.enums.DeliveryChannel;
import com.projectsaas.notification.enums.NotificationType;
import com.projectsaas.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task.assigned", groupId = "notification-service")
    public void handleTaskAssigned(Map<String, Object> eventData) {
        try {
            // Convertir le HashMap en TaskEventDto
            TaskEventDto taskEvent = objectMapper.convertValue(eventData, TaskEventDto.class);
            log.info("Received task assigned event: {}", taskEvent);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("taskTitle", taskEvent.getTaskTitle());
            metadata.put("projectName", taskEvent.getProjectName());
            metadata.put("assignedBy", taskEvent.getAssignedByUserName());
            metadata.put("userName", taskEvent.getAssignedUserName());

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
//                    .metadata(metadata)
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

        } catch (Exception e) {
            log.error("Error processing task assigned event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "task.updated", groupId = "notification-service")
    public void handleTaskUpdated(Map<String, Object> eventData) {
        try {
            TaskEventDto taskEvent = objectMapper.convertValue(eventData, TaskEventDto.class);
            log.info("Received task updated event: {}", taskEvent);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("taskTitle", taskEvent.getTaskTitle());
            metadata.put("projectName", taskEvent.getProjectName());
            metadata.put("status", taskEvent.getStatus());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(taskEvent.getAssignedUserId())
                    .title("Tâche mise à jour: " + taskEvent.getTaskTitle())
                    .message(String.format("La tâche \"%s\" a été mise à jour dans le projet %s",
                            taskEvent.getTaskTitle(), taskEvent.getProjectName()))
                    .type(NotificationType.TASK_UPDATED)
                    .channel(DeliveryChannel.WEBSOCKET)
                    .projectId(taskEvent.getProjectId())
                    .taskId(taskEvent.getTaskId())
                    .metadata(metadata)
                    .build();

            notificationService.createNotification(request);

        } catch (Exception e) {
            log.error("Error processing task updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "task.deadline.reminder", groupId = "notification-service")
    public void handleDeadlineReminder(Map<String, Object> eventData) {
        try {
            TaskEventDto taskEvent = objectMapper.convertValue(eventData, TaskEventDto.class);
            log.info("Received deadline reminder event: {}", taskEvent);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("taskTitle", taskEvent.getTaskTitle());
            metadata.put("projectName", taskEvent.getProjectName());
            metadata.put("deadline", taskEvent.getDeadline() != null ? taskEvent.getDeadline().toString() : "N/A");
            metadata.put("userName", taskEvent.getAssignedUserName());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(taskEvent.getAssignedUserId())
                    .title("Rappel d'échéance: " + taskEvent.getTaskTitle())
                    .message(String.format("La tâche \"%s\" arrive à échéance bientôt !", taskEvent.getTaskTitle()))
                    .type(NotificationType.DEADLINE_REMINDER)
                    .channel(DeliveryChannel.EMAIL)
                    .projectId(taskEvent.getProjectId())
                    .taskId(taskEvent.getTaskId())
                    .recipientEmail(taskEvent.getAssignedUserEmail())
                    .metadata(metadata)
                    .build();

            notificationService.createNotification(request);

        } catch (Exception e) {
            log.error("Error processing deadline reminder event: {}", e.getMessage(), e);
        }
    }
}