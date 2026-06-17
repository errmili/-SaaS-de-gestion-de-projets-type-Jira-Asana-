package com.projectsaas.notification.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.ProjectEventDto;
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
public class ProjectEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "project.member.added", groupId = "notification-service")
    public void handleMemberAdded(Map<String, Object> eventData) {
        try {
            ProjectEventDto projectEvent = objectMapper.convertValue(eventData, ProjectEventDto.class);
            log.info("Received member added event: {}", projectEvent);

            // Notifier chaque nouveau membre
            for (int i = 0; i < projectEvent.getMemberIds().size(); i++) {
                Long memberId = projectEvent.getMemberIds().get(i);
                String memberEmail = projectEvent.getMemberEmails().get(i);

                Map<String, String> metadata = new HashMap<>();
                metadata.put("projectName", projectEvent.getProjectName());
                metadata.put("ownerName", projectEvent.getOwnerName());
                metadata.put("memberEmail", memberEmail);

                NotificationRequest request = NotificationRequest.builder()
                        .userId(memberId)
                        .title("Invitation au projet: " + projectEvent.getProjectName())
                        .message(String.format("Vous avez été invité à rejoindre le projet \"%s\" par %s",
                                projectEvent.getProjectName(), projectEvent.getOwnerName()))
                        .type(NotificationType.PROJECT_INVITATION)
                        .channel(DeliveryChannel.EMAIL)
                        .projectId(projectEvent.getProjectId())
                        .recipientEmail(memberEmail)
                        .metadata(metadata)
                        .build();

                notificationService.createNotification(request);
            }
        } catch (Exception e) {
            log.error("Error processing member added event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "project.updated", groupId = "notification-service")
    public void handleProjectUpdated(Map<String, Object> eventData) {
        try {
            ProjectEventDto projectEvent = objectMapper.convertValue(eventData, ProjectEventDto.class);
            log.info("Received project updated event: {}", projectEvent);

            // Notifier tous les membres du projet via WebSocket
            for (Long memberId : projectEvent.getMemberIds()) {
                Map<String, String> metadata = new HashMap<>();
                metadata.put("projectName", projectEvent.getProjectName());
                metadata.put("updateType", "PROJECT_INFO");

                NotificationRequest request = NotificationRequest.builder()
                        .userId(memberId)
                        .title("Projet mis à jour")
                        .message(String.format("Le projet \"%s\" a été mis à jour", projectEvent.getProjectName()))
                        .type(NotificationType.PROJECT_UPDATED)
                        .channel(DeliveryChannel.WEBSOCKET)
                        .projectId(projectEvent.getProjectId())
                        .metadata(metadata)
                        .build();

                notificationService.createNotification(request);
            }
        } catch (Exception e) {
            log.error("Error processing project updated event: {}", e.getMessage(), e);
        }
    }
}