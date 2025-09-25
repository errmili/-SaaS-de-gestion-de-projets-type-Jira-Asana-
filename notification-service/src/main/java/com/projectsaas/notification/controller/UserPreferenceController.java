package com.projectsaas.notification.controller;

import com.projectsaas.notification.entity.UserPreference;
import com.projectsaas.notification.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPreference> getUserPreferences(@PathVariable Long userId) {
        UserPreference preferences = userPreferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserPreference> updateUserPreferences(
            @PathVariable Long userId,
            @RequestBody UserPreference preferences) {
        preferences.setUserId(userId);
        UserPreference updated = userPreferenceService.updateUserPreferences(preferences);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{userId}/reset")
    public ResponseEntity<UserPreference> resetToDefaults(@PathVariable Long userId) {
        UserPreference defaults = userPreferenceService.resetToDefaults(userId);
        return ResponseEntity.ok(defaults);
    }
}
