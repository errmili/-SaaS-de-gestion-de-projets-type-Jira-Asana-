package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.UserPreference;
import com.projectsaas.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference getUserPreferences(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    public UserPreference updateUserPreferences(UserPreference preferences) {
        log.info("Updating user preferences for user: {}", preferences.getUserId());
        return userPreferenceRepository.save(preferences);
    }

    public UserPreference resetToDefaults(Long userId) {
        log.info("Resetting preferences to defaults for user: {}", userId);

        // Supprimer les préférences existantes
        userPreferenceRepository.findByUserId(userId)
                .ifPresent(userPreferenceRepository::delete);

        // Créer de nouvelles préférences par défaut
        return createDefaultPreferences(userId);
    }

    private UserPreference createDefaultPreferences(Long userId) {
        UserPreference defaults = UserPreference.builder()
                .userId(userId)
                .emailNotifications(true)
                .pushNotifications(true)
                .websocketNotifications(true)
                .taskAssigned(true)
                .taskUpdated(true)
                .projectInvitation(true)
                .deadlineReminder(true)
                .commentMentions(true)
                .build();

        return userPreferenceRepository.save(defaults);
    }
}