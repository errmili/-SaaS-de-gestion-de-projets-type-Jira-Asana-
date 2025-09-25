package com.projectsaas.file.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShare {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @Column(name = "shared_by", nullable = false)
    private UUID sharedBy;

    @Column(name = "shared_with")
    private UUID sharedWith; // NULL pour partage public

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SharePermission permission;

    @Column(name = "share_token", unique = true)
    private String shareToken; // Token pour accès public

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Statistiques d'utilisation
    @Column(name = "access_count")
    private Integer accessCount = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Enum
    public enum SharePermission {
        READ,     // Voir seulement
        DOWNLOAD, // Voir et télécharger
        WRITE     // Voir, télécharger et modifier
    }

    // Méthodes utilitaires
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPublic() {
        return sharedWith == null;
    }

    public void incrementAccessCount() {
        accessCount = accessCount == null ? 1 : accessCount + 1;
        lastAccessedAt = LocalDateTime.now();
    }
}