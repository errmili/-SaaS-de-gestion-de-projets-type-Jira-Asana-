package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.Notification;
import com.projectsaas.notification.enums.NotificationStatus;
import com.projectsaas.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    /**
     * Traite les notifications programmées qui doivent être envoyées
     * S'exécute chaque minute
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledNotifications() {
        List<Notification> scheduledNotifications = notificationRepository
                .findByStatusAndScheduledForBefore(NotificationStatus.PENDING, LocalDateTime.now());

        if (!scheduledNotifications.isEmpty()) {
            log.info("Processing {} scheduled notifications", scheduledNotifications.size());

            for (Notification notification : scheduledNotifications) {
                try {
                    notificationService.sendNotification(notification);
                    log.debug("Sent scheduled notification: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Failed to send scheduled notification: {}", notification.getId(), e);
                }
            }
        }
    }

    /**
     * Nettoie les anciennes notifications (plus de 30 jours)
     * S'exécute tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        try {
            List<Notification> oldNotifications = notificationRepository
                    .findByCreatedAtBefore(cutoffDate);

            if (!oldNotifications.isEmpty()) {
                notificationRepository.deleteAll(oldNotifications);
                log.info("Cleaned up {} old notifications", oldNotifications.size());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old notifications", e);
        }
    }
}