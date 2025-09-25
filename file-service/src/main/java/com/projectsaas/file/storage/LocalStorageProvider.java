package com.projectsaas.file.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component("local")
@Slf4j
public class LocalStorageProvider implements StorageProvider {

    @Value("${storage.local.base-path:/var/files}")
    private String basePath;

    @Value("${storage.local.max-size:10GB}")
    private String maxSize;

    private Path baseDirectory;

    @PostConstruct
    public void init() {
        try {
            this.baseDirectory = Paths.get(basePath);
            Files.createDirectories(baseDirectory);
            log.info("LocalStorageProvider initialized with path: {}", basePath);
        } catch (IOException e) {
            log.error("Failed to initialize local storage at: {}", basePath, e);
            throw new StorageException("local", "init", "Failed to initialize local storage", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName, String tenantId) {
        try {
            // Créer structure de dossiers par tenant et date
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path tenantPath = baseDirectory.resolve(tenantId).resolve(datePath);
            Files.createDirectories(tenantPath);

            // Générer nom unique pour éviter collisions
            String extension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() +
                    (extension.isEmpty() ? "" : "." + extension);

            Path filePath = tenantPath.resolve(uniqueFileName);

            // Copier le fichier
            try (InputStream in = inputStream) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String storedPath = tenantId + "/" + datePath + "/" + uniqueFileName;
            log.debug("File stored locally: {}", storedPath);

            return storedPath;

        } catch (IOException e) {
            log.error("Failed to store file: {}", fileName, e);
            throw new StorageException("local", "store", "Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String storedPath) {
        try {
            Path filePath = baseDirectory.resolve(storedPath);

            if (!Files.exists(filePath)) {
                throw new StorageException("local", "retrieve", "File not found: " + storedPath);
            }

            // Vérifier que le fichier est dans le répertoire autorisé (sécurité)
            if (!filePath.startsWith(baseDirectory)) {
                throw new StorageException("local", "retrieve", "Path traversal attempt detected: " + storedPath);
            }

            return Files.newInputStream(filePath);

        } catch (IOException e) {
            log.error("Failed to retrieve file: {}", storedPath, e);
            throw new StorageException("local", "retrieve", "Failed to retrieve file: " + storedPath, e);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            Path filePath = baseDirectory.resolve(storedPath);

            // Vérifier sécurité
            if (!filePath.startsWith(baseDirectory)) {
                throw new StorageException("local", "delete", "Path traversal attempt detected: " + storedPath);
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("File deleted: {}", storedPath);
            } else {
                log.warn("File not found for deletion: {}", storedPath);
            }

            // Nettoyer les dossiers vides
            cleanupEmptyDirectories(filePath.getParent());

        } catch (IOException e) {
            log.error("Failed to delete file: {}", storedPath, e);
            throw new StorageException("local", "delete", "Failed to delete file: " + storedPath, e);
        }
    }

    @Override
    public String generatePresignedUrl(String storedPath, Duration expiration) {
        // Pour stockage local, on génère un token temporaire
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + expiration.toMillis();

        // En production, stocker ces tokens en cache (Redis)
        // Pour l'instant, on retourne une URL avec les paramètres
        return String.format("/api/files/download/temp/%s?path=%s&expires=%d",
                token, storedPath, expiresAt);
    }

    @Override
    public boolean exists(String storedPath) {
        try {
            Path filePath = baseDirectory.resolve(storedPath);
            return Files.exists(filePath) && filePath.startsWith(baseDirectory);
        } catch (Exception e) {
            log.error("Error checking if file exists: {}", storedPath, e);
            return false;
        }
    }

    @Override
    public long getFileSize(String storedPath) {
        try {
            Path filePath = baseDirectory.resolve(storedPath);
            if (!filePath.startsWith(baseDirectory)) {
                return 0;
            }
            return Files.size(filePath);
        } catch (IOException e) {
            log.error("Failed to get file size: {}", storedPath, e);
            return 0;
        }
    }

    @Override
    public void copy(String sourcePath, String destPath) {
        try {
            Path source = baseDirectory.resolve(sourcePath);
            Path dest = baseDirectory.resolve(destPath);

            // Vérifier sécurité
            if (!source.startsWith(baseDirectory) || !dest.startsWith(baseDirectory)) {
                throw new StorageException("local", "copy", "Path traversal attempt detected");
            }

            Files.createDirectories(dest.getParent());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);

            log.debug("File copied from {} to {}", sourcePath, destPath);

        } catch (IOException e) {
            log.error("Failed to copy file from {} to {}", sourcePath, destPath, e);
            throw new StorageException("local", "copy",
                    String.format("Failed to copy file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public void move(String sourcePath, String destPath) {
        try {
            Path source = baseDirectory.resolve(sourcePath);
            Path dest = baseDirectory.resolve(destPath);

            // Vérifier sécurité
            if (!source.startsWith(baseDirectory) || !dest.startsWith(baseDirectory)) {
                throw new StorageException("local", "move", "Path traversal attempt detected");
            }

            Files.createDirectories(dest.getParent());
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);

            // Nettoyer dossier source s'il est vide
            cleanupEmptyDirectories(source.getParent());

            log.debug("File moved from {} to {}", sourcePath, destPath);

        } catch (IOException e) {
            log.error("Failed to move file from {} to {}", sourcePath, destPath, e);
            throw new StorageException("local", "move",
                    String.format("Failed to move file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public StorageStats getStats() {
        try {
            if (!Files.exists(baseDirectory)) {
                Files.createDirectories(baseDirectory);
            }

            // Calculer l'espace utilisé et compter les fichiers
            final long[] stats = new long[2]; // [usedSpace, fileCount]

            Files.walk(baseDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            stats[0] += Files.size(path); // usedSpace
                            stats[1]++; // fileCount
                        } catch (IOException e) {
                            log.warn("Error reading file size: {}", path, e);
                        }
                    });

            // Espace disponible
            long availableSpace = Files.getFileStore(baseDirectory).getUsableSpace();

            StorageStats storageStats = StorageStats.builder()
                    .totalFiles(stats[1])
                    .totalSize(stats[0])
                    .usedSpace(stats[0])
                    .availableSpace(availableSpace)
                    .provider("LOCAL")
                    .healthy(true)
                    .statusMessage("Local storage operational")
                    .build();

            storageStats.calculateUsagePercentage();
            storageStats.formatSizes();

            return storageStats;

        } catch (IOException e) {
            log.error("Failed to get storage stats", e);
            return StorageStats.builder()
                    .provider("LOCAL")
                    .healthy(false)
                    .statusMessage("Error getting storage stats: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return Files.exists(baseDirectory) &&
                    Files.isWritable(baseDirectory) &&
                    Files.isReadable(baseDirectory);
        } catch (Exception e) {
            log.error("Health check failed for local storage", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }

    // Méthodes utilitaires privées
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private void cleanupEmptyDirectories(Path directory) {
        try {
            if (directory != null &&
                    Files.exists(directory) &&
                    Files.isDirectory(directory) &&
                    !directory.equals(baseDirectory)) {

                // Vérifier si le dossier est vide
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
                    if (!dirStream.iterator().hasNext()) {
                        Files.delete(directory);
                        log.debug("Deleted empty directory: {}", directory);

                        // Récursif pour le parent
                        cleanupEmptyDirectories(directory.getParent());
                    }
                }
            }
        } catch (IOException e) {
            log.debug("Could not cleanup directory: {}", directory, e);
        }
    }
}