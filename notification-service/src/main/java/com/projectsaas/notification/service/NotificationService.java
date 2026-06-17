package com.projectsaas.notification.service;

import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.dto.NotificationResponse;
import com.projectsaas.notification.entity.Notification;
import com.projectsaas.notification.entity.UserPreference;
import com.projectsaas.notification.enums.DeliveryChannel;
import com.projectsaas.notification.enums.NotificationStatus;
import com.projectsaas.notification.enums.NotificationType;
import com.projectsaas.notification.repository.NotificationRepository;
import com.projectsaas.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EmailService emailService;
    private final WebSocketService webSocketService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Transactional
    public Notification createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        // 1. Créer la notification
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .projectId(request.getProjectId())
                .taskId(request.getTaskId())
                .recipientEmail(request.getRecipientEmail())
//                .metadata(request.getMetadata())
                .build();

        // 2. Sauvegarder IMMÉDIATEMENT
        notification = notificationRepository.save(notification);
        log.info("✅ Notification saved with ID: {}", notification.getId());

        // 3. Envoyer (SANS propager l'exception)
        try {
            sendNotificationNow(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("✅ Notification sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        }

        // 4. Mettre à jour le statut
        notification = notificationRepository.save(notification);
        log.info("✅ Final status: {} for notification: {}", notification.getStatus(), notification.getId());


        if (DeliveryChannel.WEBSOCKET.equals(notification.getChannel())) {
            publishNotificationViaWebSocket(notification);
        }

        return notification;
    }

    private void publishNotificationViaWebSocket(Notification notification) {
        try {
            // ✅ CORRIGÉ - Créer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("id", notification.getId());
            response.put("userId", notification.getUserId());
            response.put("title", notification.getTitle());
            response.put("message", notification.getMessage());
            response.put("type", notification.getType());
            response.put("status", notification.getStatus());
            response.put("channel", notification.getChannel());
            response.put("projectId", notification.getProjectId());
            response.put("taskId", notification.getTaskId());
            response.put("createdAt", notification.getCreatedAt());

            // ✅ CRITIQUE - Utiliser convertAndSendToUser au lieu de convertAndSend
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(notification.getUserId()),  // userId en String
                    "/queue/notifications",                     // destination (SANS /user/)
                    response
            );

            log.info("📤 Notification sent via WebSocket to user: {}", notification.getUserId());
        } catch (Exception e) {
            log.error("❌ Error sending notification via WebSocket", e);
        }
    }
    // Méthode publique pour les appels externes (ScheduledNotificationService, etc.)
    @Transactional
    public void sendNotification(Notification notification) {
        try {
            sendNotificationNow(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("✅ Notification sent: {}", notification.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    // Méthode privée d'envoi SANS sauvegarde
    private void sendNotificationNow(Notification notification) {
        switch (notification.getChannel()) {
            case EMAIL -> emailService.sendEmail(notification);
            case WEBSOCKET -> webSocketService.sendNotification(notification);
            case PUSH -> sendPushNotification(notification);
            default -> log.warn("Unknown delivery channel: {}", notification.getChannel());
        }
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId)
                .ifPresent(notification -> {
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    private void sendPushNotification(Notification notification) {
        log.info("📱 Push notification sent to user: {}", notification.getUserId());
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndStatus(userId, NotificationStatus.SENT);

        for (Notification notification : unreadNotifications) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }
}