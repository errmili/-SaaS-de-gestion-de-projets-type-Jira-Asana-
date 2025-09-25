package com.projectsaas.file.repository;

import com.projectsaas.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    // Métadonnées par fichier
    Optional<FileMetadata> findByFileId(UUID fileId);

    // Fichiers avec GPS
    @Query("SELECT fm FROM FileMetadata fm " +
            "WHERE fm.file.tenantId = :tenantId " +
            "AND fm.gpsLatitude IS NOT NULL " +
            "AND fm.gpsLongitude IS NOT NULL")
    List<FileMetadata> findFilesWithGpsData(@Param("tenantId") UUID tenantId);

    // Fichiers par appareil photo
    List<FileMetadata> findByCameraMakeAndCameraModel(String make, String model);

    // Fichiers avec thumbnails manquants
    @Query("SELECT fm FROM FileMetadata fm " +
            "WHERE fm.file.tenantId = :tenantId " +
            "AND fm.file.contentType LIKE 'image/%' " +
            "AND (fm.thumbnailSmall IS NULL OR fm.thumbnailMedium IS NULL)")
    List<FileMetadata> findFilesNeedingThumbnails(@Param("tenantId") UUID tenantId);
}