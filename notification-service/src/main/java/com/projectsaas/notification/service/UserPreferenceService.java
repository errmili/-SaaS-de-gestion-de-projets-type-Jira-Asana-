package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.UserPreference;
import com.projectsaas.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * Récupérer les préférences d'un utilisateur
     * Si elles n'existent pas, créer des préférences par défaut
     */
    public UserPreference getUserPreferences(Long userId) {
        log.info("Getting preferences for user: {}", userId);
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Mettre à jour les préférences d'un utilisateur
     * CORRECTIF: Récupère l'entité existante AVANT de modifier
     */
    @Transactional
    public UserPreference updateUserPreferences(UserPreference newPreferences) {
        log.info("Updating user preferences for user: {}", newPreferences.getUserId());

        // 🔧 FIX: Récupérer l'entité existante
        UserPreference existingPreferences = userPreferenceRepository
                .findByUserId(newPreferences.getUserId())
                .orElseGet(() -> createDefaultPreferences(newPreferences.getUserId()));

        // Mettre à jour les champs
        existingPreferences.setEmailNotifications(newPreferences.getEmailNotifications());
        existingPreferences.setPushNotifications(newPreferences.getPushNotifications());
        existingPreferences.setWebsocketNotifications(newPreferences.getWebsocketNotifications());
        existingPreferences.setTaskAssigned(newPreferences.getTaskAssigned());
        existingPreferences.setTaskUpdated(newPreferences.getTaskUpdated());
        existingPreferences.setProjectInvitation(newPreferences.getProjectInvitation());
        existingPreferences.setDeadlineReminder(newPreferences.getDeadlineReminder());
        existingPreferences.setCommentMentions(newPreferences.getCommentMentions());
        existingPreferences.setUpdatedAt(LocalDateTime.now());

        // Sauvegarder (UPDATE au lieu de INSERT)
        UserPreference saved = userPreferenceRepository.save(existingPreferences);
        log.info("Preferences updated successfully for user: {}", saved.getUserId());

        return saved;
    }

    /**
     * Réinitialiser les préférences aux valeurs par défaut
     */
    @Transactional
    public UserPreference resetToDefaults(Long userId) {
        log.info("Resetting preferences to defaults for user: {}", userId);

        // Récupérer ou créer les préférences
        UserPreference preferences = userPreferenceRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    UserPreference newPref = new UserPreference();
                    newPref.setUserId(userId);
                    return newPref;
                });

        // Réinitialiser aux valeurs par défaut (tout activé)
        preferences.setEmailNotifications(true);
        preferences.setPushNotifications(true);
        preferences.setWebsocketNotifications(true);
        preferences.setTaskAssigned(true);
        preferences.setTaskUpdated(true);
        preferences.setProjectInvitation(true);
        preferences.setDeadlineReminder(true);
        preferences.setCommentMentions(true);
        preferences.setUpdatedAt(LocalDateTime.now());

        return userPreferenceRepository.save(preferences);
    }

    /**
     * Créer des préférences par défaut pour un nouvel utilisateur
     */
    private UserPreference createDefaultPreferences(Long userId) {
        log.info("Creating default preferences for user: {}", userId);

        UserPreference preferences = UserPreference.builder()
                .userId(userId)
                .emailNotifications(true)
                .pushNotifications(true)
                .websocketNotifications(true)
                .taskAssigned(true)
                .taskUpdated(true)
                .projectInvitation(true)
                .deadlineReminder(true)
                .commentMentions(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userPreferenceRepository.save(preferences);
    }

    /**
     * Vérifier si un type de notification est activé pour un utilisateur
     */
    public boolean isNotificationEnabled(Long userId, String notificationType, String channel) {
        UserPreference preferences = getUserPreferences(userId);

        // Vérifier si le channel est activé
        boolean channelEnabled = switch (channel.toUpperCase()) {
            case "EMAIL" -> preferences.getEmailNotifications();
            case "PUSH" -> preferences.getPushNotifications();
            case "WEBSOCKET" -> preferences.getWebsocketNotifications();
            default -> true; // Par défaut, autoriser
        };

        if (!channelEnabled) {
            log.info("Channel {} is disabled for user: {}", channel, userId);
            return false;
        }

        // Vérifier si le type de notification est activé
        boolean typeEnabled = switch (notificationType.toUpperCase()) {
            case "TASK_ASSIGNED" -> preferences.getTaskAssigned();
            case "TASK_UPDATED" -> preferences.getTaskUpdated();
            case "PROJECT_INVITATION" -> preferences.getProjectInvitation();
            case "DEADLINE_REMINDER" -> preferences.getDeadlineReminder();
            case "MENTION" -> preferences.getCommentMentions();
            default -> true; // Par défaut, autoriser les autres types
        };

        if (!typeEnabled) {
            log.info("Notification type {} is disabled for user: {}", notificationType, userId);
            return false;
        }

        return true;
    }
}