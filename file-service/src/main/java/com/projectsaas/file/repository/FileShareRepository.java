package com.projectsaas.file.repository;

import com.projectsaas.file.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, UUID> {

    // Partage par token
    Optional<FileShare> findByShareTokenAndIsActiveTrue(String shareToken);

    // Partages d'un fichier
    List<FileShare> findByFileIdAndIsActiveTrueOrderByCreatedAtDesc(UUID fileId);

    // Fichiers partagés par un utilisateur
    @Query("SELECT fs FROM FileShare fs " +
            "WHERE fs.sharedBy = :userId " +
            "AND fs.file.tenantId = :tenantId " +
            "AND fs.isActive = true " +
            "ORDER BY fs.createdAt DESC")
    List<FileShare> findSharedByUser(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    // Fichiers partagés avec un utilisateur
    @Query("SELECT fs FROM FileShare fs " +
            "WHERE fs.sharedWith = :userId " +
            "AND fs.file.tenantId = :tenantId " +
            "AND fs.isActive = true " +
            "AND (fs.expiresAt IS NULL OR fs.expiresAt > :now) " +
            "ORDER BY fs.createdAt DESC")
    List<FileShare> findSharedWithUser(@Param("userId") UUID userId,
                                       @Param("tenantId") UUID tenantId,
                                       @Param("now") LocalDateTime now);

    // Partages publics actifs
    @Query("SELECT fs FROM FileShare fs " +
            "WHERE fs.sharedWith IS NULL " +
            "AND fs.file.tenantId = :tenantId " +
            "AND fs.isActive = true " +
            "AND (fs.expiresAt IS NULL OR fs.expiresAt > :now) " +
            "ORDER BY fs.createdAt DESC")
    List<FileShare> findPublicShares(@Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    // Partages expirés
    @Query("SELECT fs FROM FileShare fs " +
            "WHERE fs.expiresAt IS NOT NULL " +
            "AND fs.expiresAt < :now " +
            "AND fs.isActive = true")
    List<FileShare> findExpiredShares(@Param("now") LocalDateTime now);

    // Vérifier permission sur fichier
    @Query("SELECT fs FROM FileShare fs " +
            "WHERE fs.file.id = :fileId " +
            "AND (fs.sharedWith = :userId OR fs.sharedWith IS NULL) " +
            "AND fs.isActive = true " +
            "AND (fs.expiresAt IS NULL OR fs.expiresAt > :now)")
    List<FileShare> findActiveSharesForFileAndUser(@Param("fileId") UUID fileId,
                                                   @Param("userId") UUID userId,
                                                   @Param("now") LocalDateTime now);
}