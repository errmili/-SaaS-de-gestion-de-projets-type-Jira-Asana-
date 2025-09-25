package com.projectsaas.notification.service;

import com.projectsaas.notification.dto.NotificationRequest;
import com.projectsaas.notification.entity.Notification;
import com.projectsaas.notification.entity.UserPreference;
import com.projectsaas.notification.enums.DeliveryChannel;
import com.projectsaas.notification.enums.NotificationStatus;
import com.projectsaas.notification.enums.NotificationType;
import com.projectsaas.notification.repository.NotificationRepository;
import com.projectsaas.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EmailService emailService;
    private final WebSocketService webSocketService;

    public Notification createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        // Vérifier les préférences utilisateur
        if (!shouldSendNotification(request)) {
            log.info("Notification skipped due to user preferences: {}", request);
            return null;
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .status(NotificationStatus.PENDING)
                .channel(request.getChannel())
                .metadata(request.getMetadata())
                .projectId(request.getProjectId())
                .taskId(request.getTaskId())
                .recipientEmail(request.getRecipientEmail())
                .scheduledFor(request.getScheduledFor())
                .build();

        notification = notificationRepository.save(notification);

        // Envoyer immédiatement si pas de planification
        if (request.getScheduledFor() == null ||
                request.getScheduledFor().isBefore(LocalDateTime.now())) {
            sendNotification(notification);
        }

        return notification;
    }

    private boolean shouldSendNotification(NotificationRequest request) {
        UserPreference preferences = userPreferenceRepository.findByUserId(request.getUserId())
                .orElse(UserPreference.builder()
                        .userId(request.getUserId())
                        .build());

        // Vérifier le canal de delivery
        return switch (request.getChannel()) {
            case EMAIL -> preferences.getEmailNotifications();
            case WEBSOCKET -> preferences.getWebsocketNotifications();
            case PUSH -> preferences.getPushNotifications();
            default -> true;
        } && shouldSendForType(request.getType(), preferences);
    }

    private boolean shouldSendForType(NotificationType type, UserPreference preferences) {
        return switch (type) {
            case TASK_ASSIGNED -> preferences.getTaskAssigned();
            case TASK_UPDATED -> preferences.getTaskUpdated();
            case PROJECT_INVITATION -> preferences.getProjectInvitation();
            case DEADLINE_REMINDER -> preferences.getDeadlineReminder();
            case MENTION -> preferences.getCommentMentions();
            default -> true;
        };
    }

    public void sendNotification(Notification notification) {
        try {
            switch (notification.getChannel()) {
                case EMAIL -> emailService.sendEmail(notification);
                case WEBSOCKET -> webSocketService.sendNotification(notification);
                case PUSH -> sendPushNotification(notification);
                default -> log.warn("Unknown delivery channel: {}", notification.getChannel());
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
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
        // TODO: Implémenter l'envoi de push notifications
        log.info("Push notification sent to user: {}", notification.getUserId());
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