package com.projectsaas.file.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * Interface pour les différents providers de stockage
 * Supporte Local, S3, MinIO, etc.
 */
public interface StorageProvider {

    /**
     * Stocker un fichier
     * @param inputStream Flux du fichier
     * @param fileName Nom du fichier original
     * @param tenantId ID du tenant
     * @return Chemin de stockage généré
     */
    String store(InputStream inputStream, String fileName, String tenantId);

    /**
     * Récupérer un fichier
     * @param storedPath Chemin de stockage
     * @return Flux du fichier
     */
    InputStream retrieve(String storedPath);

    /**
     * Supprimer un fichier
     * @param storedPath Chemin de stockage
     */
    void delete(String storedPath);

    /**
     * Générer URL de téléchargement temporaire
     * @param storedPath Chemin de stockage
     * @param expiration Durée de validité
     * @return URL temporaire
     */
    String generatePresignedUrl(String storedPath, Duration expiration);

    /**
     * Vérifier si fichier existe
     * @param storedPath Chemin de stockage
     * @return true si existe
     */
    boolean exists(String storedPath);

    /**
     * Obtenir taille du fichier
     * @param storedPath Chemin de stockage
     * @return Taille en bytes
     */
    long getFileSize(String storedPath);

    /**
     * Copier un fichier
     * @param sourcePath Chemin source
     * @param destPath Chemin destination
     */
    void copy(String sourcePath, String destPath);

    /**
     * Déplacer un fichier
     * @param sourcePath Chemin source
     * @param destPath Chemin destination
     */
    void move(String sourcePath, String destPath);

    /**
     * Obtenir statistiques de stockage
     * @return Statistiques
     */
    StorageStats getStats();

    /**
     * Vérifier la santé du provider
     * @return true si fonctionnel
     */
    boolean isHealthy();

    /**
     * Nom du provider
     * @return Nom du provider (local, s3, minio)
     */
    String getProviderName();
}
