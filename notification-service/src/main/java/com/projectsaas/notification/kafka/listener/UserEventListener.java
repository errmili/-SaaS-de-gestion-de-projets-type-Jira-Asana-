package com.projectsaas.notification.kafka.listener;

import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.UserEventDto;
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
public class UserEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.profile.updated", groupId = "notification-service")
    public void handleUserProfileUpdated(UserEventDto userEvent) {
        log.info("Received user profile updated event: {}", userEvent);

        NotificationRequest request = NotificationRequest.builder()
                .userId(userEvent.getUserId())
                .title("Profil mis à jour")
                .message("Votre profil a été mis à jour avec succès")
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .channel(DeliveryChannel.WEBSOCKET)
                .metadata(Map.of(
                        "userName", userEvent.getUserName(),
                        "updateType", "PROFILE"
                ))
                .build();

        notificationService.createNotification(request);
    }

    @KafkaListener(topics = "user.password.changed", groupId = "notification-service")
    public void handlePasswordChanged(UserEventDto userEvent) {
        log.info("Received password changed event: {}", userEvent);

        NotificationRequest request = NotificationRequest.builder()
                .userId(userEvent.getUserId())
                .title("Mot de passe modifié")
                .message("Votre mot de passe a été modifié avec succès. Si ce n'était pas vous, contactez immédiatement le support.")
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .channel(DeliveryChannel.EMAIL)
                .recipientEmail(userEvent.getUserEmail())
                .metadata(Map.of(
                        "userName", userEvent.getUserName(),
                        "securityAlert", "true"
                ))
                .build();

        notificationService.createNotification(request);
    }
}
