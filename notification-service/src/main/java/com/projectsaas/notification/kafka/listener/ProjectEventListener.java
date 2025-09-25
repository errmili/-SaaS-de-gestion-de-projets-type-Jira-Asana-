package com.projectsaas.notification.kafka.listener;

import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.ProjectEventDto;
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
public class ProjectEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "project.member.added", groupId = "notification-service")
    public void handleMemberAdded(ProjectEventDto projectEvent) {
        log.info("Received member added event: {}", projectEvent);

        // Notifier chaque nouveau membre
        for (int i = 0; i < projectEvent.getMemberIds().size(); i++) {
            Long memberId = projectEvent.getMemberIds().get(i);
            String memberEmail = projectEvent.getMemberEmails().get(i);

            NotificationRequest request = NotificationRequest.builder()
                    .userId(memberId)
                    .title("Invitation au projet: " + projectEvent.getProjectName())
                    .message(String.format("Vous avez été invité à rejoindre le projet \"%s\" par %s",
                            projectEvent.getProjectName(), projectEvent.getOwnerName()))
                    .type(NotificationType.PROJECT_INVITATION)
                    .channel(DeliveryChannel.EMAIL)
                    .projectId(projectEvent.getProjectId())
                    .recipientEmail(memberEmail)
                    .metadata(Map.of(
                            "projectName", projectEvent.getProjectName(),
                            "ownerName", projectEvent.getOwnerName(),
                            "memberEmail", memberEmail
                    ))
                    .build();

            notificationService.createNotification(request);
        }
    }

    @KafkaListener(topics = "project.updated", groupId = "notification-service")
    public void handleProjectUpdated(ProjectEventDto projectEvent) {
        log.info("Received project updated event: {}", projectEvent);

        // Notifier tous les membres du projet via WebSocket
        for (Long memberId : projectEvent.getMemberIds()) {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(memberId)
                    .title("Projet mis à jour")
                    .message(String.format("Le projet \"%s\" a été mis à jour", projectEvent.getProjectName()))
                    .type(NotificationType.PROJECT_UPDATED)
                    .channel(DeliveryChannel.WEBSOCKET)
                    .projectId(projectEvent.getProjectId())
                    .metadata(Map.of(
                            "projectName", projectEvent.getProjectName(),
                            "updateType", "PROJECT_INFO"
                    ))
                    .build();

            notificationService.createNotification(request);
        }
    }
}