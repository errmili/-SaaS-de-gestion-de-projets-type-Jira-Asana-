package com.projectsaas.file.repository;

import com.projectsaas.file.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {

    // Toutes les versions d'un fichier
    List<FileVersion> findByFileIdOrderByVersionNumberDesc(UUID fileId);

    // Version spécifique d'un fichier
    Optional<FileVersion> findByFileIdAndVersionNumber(UUID fileId, Integer versionNumber);

    // Dernière version d'un fichier
    @Query("SELECT fv FROM FileVersion fv " +
            "WHERE fv.file.id = :fileId " +
            "ORDER BY fv.versionNumber DESC " +
            "LIMIT 1")
    Optional<FileVersion> findLatestVersion(@Param("fileId") UUID fileId);

    // Prochain numéro de version
    @Query("SELECT COALESCE(MAX(fv.versionNumber), 0) + 1 FROM FileVersion fv " +
            "WHERE fv.file.id = :fileId")
    Integer getNextVersionNumber(@Param("fileId") UUID fileId);

    // Versions créées par un utilisateur
    List<FileVersion> findByUploadedByOrderByUploadedAtDesc(UUID uploadedBy);

    // Taille totale des versions d'un fichier
    @Query("SELECT SUM(fv.fileSize) FROM FileVersion fv WHERE fv.file.id = :fileId")
    Long getTotalVersionsSize(@Param("fileId") UUID fileId);

    // Compter versions par fichier
    @Query("SELECT COUNT(fv) FROM FileVersion fv WHERE fv.file.id = :fileId")
    Long countVersionsByFile(@Param("fileId") UUID fileId);
}
