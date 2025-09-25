package com.projectsaas.file.repository;
import com.projectsaas.file.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    // ========== MÉTHODES EXISTANTES DANS VOTRE PROJET ==========

    Optional<FileEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<FileEntity> findByTenantIdAndStatusOrderByUploadedAtDesc(
            UUID tenantId, FileEntity.FileStatus status, Pageable pageable);

    List<FileEntity> findByFolderIdAndTenantIdAndStatusOrderByOriginalNameAsc(
            UUID folderId, UUID tenantId, FileEntity.FileStatus status);

    // ✅ CORRIGÉ : FolderIsNull → FolderIdIsNull
    List<FileEntity> findByFolderIdIsNullAndTenantIdAndStatusOrderByOriginalNameAsc(
            UUID tenantId, FileEntity.FileStatus status);

    List<FileEntity> findByTenantIdAndUploadedByAndStatusOrderByUploadedAtDesc(
            UUID tenantId, UUID uploadedBy, FileEntity.FileStatus status);

    List<FileEntity> findByTenantIdAndEntityTypeAndEntityIdAndStatusOrderByUploadedAtDesc(
            UUID tenantId, FileEntity.EntityType entityType, UUID entityId, FileEntity.FileStatus status);

    List<FileEntity> findByTenantIdAndIsPublicTrueAndStatusOrderByUploadedAtDesc(
            UUID tenantId, FileEntity.FileStatus status);

    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId " +
            "AND f.status = :status " +
            "AND (LOWER(f.originalName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY f.uploadedAt DESC")
    List<FileEntity> searchByName(@Param("tenantId") UUID tenantId,
                                  @Param("status") FileEntity.FileStatus status,
                                  @Param("search") String search);

    List<FileEntity> findByTenantIdAndContentTypeStartingWithAndStatusOrderByUploadedAtDesc(
            UUID tenantId, String contentTypePrefix, FileEntity.FileStatus status);

    List<FileEntity> findTop10ByTenantIdAndStatusOrderByUploadedAtDesc(
            UUID tenantId, FileEntity.FileStatus status);

    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId " +
            "AND f.status = :status " +
            "AND f.fileSize > :minSize " +
            "ORDER BY f.fileSize DESC")
    List<FileEntity> findLargeFiles(@Param("tenantId") UUID tenantId,
                                    @Param("status") FileEntity.FileStatus status,
                                    @Param("minSize") Long minSize);

    List<FileEntity> findByVirusScanStatusAndUploadedAtBefore(
            FileEntity.VirusScanStatus scanStatus, LocalDateTime before);

    @Query("SELECT SUM(f.fileSize) FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId AND f.status = :status")
    Long sumFileSizeByTenant(@Param("tenantId") UUID tenantId,
                             @Param("status") FileEntity.FileStatus status);

    @Query("SELECT COUNT(f) FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId AND f.status = :status")
    Long countFilesByTenant(@Param("tenantId") UUID tenantId,
                            @Param("status") FileEntity.FileStatus status);

    @Query("SELECT f.contentType, COUNT(f), SUM(f.fileSize) FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId AND f.status = :status " +
            "GROUP BY f.contentType " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> getFileStatsByContentType(@Param("tenantId") UUID tenantId,
                                             @Param("status") FileEntity.FileStatus status);

    boolean existsByTenantIdAndChecksumAndStatus(UUID tenantId, String checksum, FileEntity.FileStatus status);

    List<FileEntity> findByTenantIdAndEntityTypeIsNullAndStatusOrderByUploadedAtDesc(
            UUID tenantId, FileEntity.FileStatus status);

    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.status = 'DELETED' " +
            "AND f.updatedAt < :cutoffDate")
    List<FileEntity> findExpiredDeletedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== NOUVELLES MÉTHODES POUR LA COMPATIBILITÉ ==========

    // Pour la recherche avec pagination
    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId " +
            "AND f.status = :status " +
            "AND (LOWER(f.originalName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY f.uploadedAt DESC")
    Page<FileEntity> searchFiles(@Param("search") String search,
                                 @Param("tenantId") UUID tenantId,
                                 @Param("status") FileEntity.FileStatus status,
                                 Pageable pageable);

    // Version simplifiée pour getFolderFiles
    List<FileEntity> findByFolderIdAndTenantIdAndStatus(UUID folderId, UUID tenantId, FileEntity.FileStatus status);

    // Version simplifiée pour les fichiers root
    List<FileEntity> findByFolderIdIsNullAndTenantIdAndStatus(UUID tenantId, FileEntity.FileStatus status);

    // Version avec pagination sans recherche
    Page<FileEntity> findByTenantIdAndStatus(UUID tenantId, FileEntity.FileStatus status, Pageable pageable);

    // Version avec pagination et dossier
    Page<FileEntity> findByTenantIdAndFolderIdAndStatus(UUID tenantId, UUID folderId, FileEntity.FileStatus status, Pageable pageable);

    // Pour les recherches par nom (sans pagination)
    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.tenantId = :tenantId " +
            "AND f.status = :status " +
            "AND LOWER(f.originalName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY f.uploadedAt DESC")
    List<FileEntity> searchFilesByName(@Param("query") String query,
                                       @Param("tenantId") UUID tenantId,
                                       @Param("status") FileEntity.FileStatus status);

    // Vérification d'existence par nom original
    boolean existsByOriginalNameAndTenantIdAndFolderIdAndStatus(
            String originalName, UUID tenantId, UUID folderId, FileEntity.FileStatus status);
}