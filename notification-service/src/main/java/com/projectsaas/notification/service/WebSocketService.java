package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Notification notification) {
        try {
            Map<String, Object> payload = Map.of(
                    "id", notification.getId(),
                    "title", notification.getTitle(),
                    "message", notification.getMessage(),
                    "type", notification.getType(),
                    "projectId", notification.getProjectId() != null ? notification.getProjectId() : "",
                    "taskId", notification.getTaskId() != null ? notification.getTaskId() : "",
                    "timestamp", notification.getCreatedAt()
            );

            // Envoyer à l'utilisateur spécifique
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    payload
            );

            log.info("WebSocket notification sent to user: {}", notification.getUserId());

        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user: {}", notification.getUserId(), e);
            throw new RuntimeException("Failed to send WebSocket notification", e);
        }
    }

    public void sendToProject(Long projectId, Map<String, Object> message) {
        messagingTemplate.convertAndSend("/topic/project/" + projectId, message);
        log.info("WebSocket message sent to project: {}", projectId);
    }
}