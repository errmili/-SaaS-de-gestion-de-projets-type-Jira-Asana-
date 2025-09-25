package com.projectsaas.file.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Storage Provider pour les tests
 * Stocke les fichiers en mémoire
 */
@Component("mock")
@ConditionalOnProperty(name = "storage.provider", havingValue = "mock")
@Slf4j
public class MockStorageProvider implements StorageProvider {

    private final Map<String, MockFile> storage = new ConcurrentHashMap<>();
    private boolean simulateFailures = false;
    private int operationDelay = 0; // Délai simulé en ms

    // Classe interne pour stocker les fichiers mockés
    private static class MockFile {
        final byte[] content;
        final String fileName;
        final String tenantId;
        final LocalDateTime createdAt;
        final String contentType;

        MockFile(byte[] content, String fileName, String tenantId, String contentType) {
            this.content = content;
            this.fileName = fileName;
            this.tenantId = tenantId;
            this.createdAt = LocalDateTime.now();
            this.contentType = contentType;
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName, String tenantId) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "store", "Simulated failure");
        }

        try {
            // Lire le contenu dans un tableau de bytes
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            // Générer le chemin de stockage
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String extension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() +
                    (extension.isEmpty() ? "" : "." + extension);

            String storedPath = tenantId + "/" + datePath + "/" + uniqueFileName;

            // Stocker en mémoire
            MockFile mockFile = new MockFile(
                    buffer.toByteArray(),
                    fileName,
                    tenantId,
                    getContentType(fileName)
            );

            storage.put(storedPath, mockFile);

            log.debug("Mock file stored: {} (size: {} bytes)", storedPath, mockFile.content.length);
            return storedPath;

        } catch (IOException e) {
            throw new StorageException("mock", "store", "Failed to read input stream", e);
        }
    }

    @Override
    public InputStream retrieve(String storedPath) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "retrieve", "Simulated failure");
        }

        MockFile mockFile = storage.get(storedPath);
        if (mockFile == null) {
            throw new StorageException("mock", "retrieve", "File not found: " + storedPath);
        }

        log.debug("Mock file retrieved: {} (size: {} bytes)", storedPath, mockFile.content.length);
        return new ByteArrayInputStream(mockFile.content);
    }

    @Override
    public void delete(String storedPath) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "delete", "Simulated failure");
        }

        MockFile removed = storage.remove(storedPath);
        if (removed != null) {
            log.debug("Mock file deleted: {}", storedPath);
        } else {
            log.warn("Mock file not found for deletion: {}", storedPath);
        }
    }

    @Override
    public String generatePresignedUrl(String storedPath, Duration expiration) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "presign", "Simulated failure");
        }

        // Pour les tests, on génère une URL mockée
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + expiration.toMillis();

        String url = String.format("/mock/download/%s?expires=%d", token, expiresAt);
        log.debug("Mock presigned URL generated: {}", url);

        return url;
    }

    @Override
    public boolean exists(String storedPath) {
        simulateDelay();

        boolean exists = storage.containsKey(storedPath);
        log.debug("Mock file exists check: {} -> {}", storedPath, exists);

        return exists;
    }

    @Override
    public long getFileSize(String storedPath) {
        simulateDelay();

        MockFile mockFile = storage.get(storedPath);
        if (mockFile == null) {
            return 0;
        }

        return mockFile.content.length;
    }

    @Override
    public void copy(String sourcePath, String destPath) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "copy", "Simulated failure");
        }

        MockFile source = storage.get(sourcePath);
        if (source == null) {
            throw new StorageException("mock", "copy", "Source file not found: " + sourcePath);
        }

        // Créer une copie
        MockFile copy = new MockFile(
                source.content.clone(),
                source.fileName,
                source.tenantId,
                source.contentType
        );

        storage.put(destPath, copy);
        log.debug("Mock file copied from {} to {}", sourcePath, destPath);
    }

    @Override
    public void move(String sourcePath, String destPath) {
        simulateDelay();

        if (simulateFailures) {
            throw new StorageException("mock", "move", "Simulated failure");
        }

        MockFile source = storage.remove(sourcePath);
        if (source == null) {
            throw new StorageException("mock", "move", "Source file not found: " + sourcePath);
        }

        storage.put(destPath, source);
        log.debug("Mock file moved from {} to {}", sourcePath, destPath);
    }

    @Override
    public StorageStats getStats() {
        simulateDelay();

        long totalFiles = storage.size();
        long totalSize = storage.values().stream()
                .mapToLong(file -> file.content.length)
                .sum();

        StorageStats stats = StorageStats.builder()
                .provider("MOCK")
                .totalFiles(totalFiles)
                .totalSize(totalSize)
                .usedSpace(totalSize)
                .availableSpace(Long.MAX_VALUE) // Illimité en mémoire
                .healthy(true)
                .statusMessage("Mock storage operational")
                .build();

        stats.calculateUsagePercentage();
        stats.formatSizes();

        return stats;
    }

    @Override
    public boolean isHealthy() {
        return !simulateFailures;
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    // Méthodes utilitaires pour les tests
    public void enableFailureSimulation() {
        this.simulateFailures = true;
        log.info("Mock storage failure simulation enabled");
    }

    public void disableFailureSimulation() {
        this.simulateFailures = false;
        log.info("Mock storage failure simulation disabled");
    }

    public void setOperationDelay(int delayMs) {
        this.operationDelay = delayMs;
        log.info("Mock storage operation delay set to {} ms", delayMs);
    }

    public void clearStorage() {
        storage.clear();
        log.info("Mock storage cleared");
    }

    public int getStoredFileCount() {
        return storage.size();
    }

    public boolean hasFile(String storedPath) {
        return storage.containsKey(storedPath);
    }

    public MockFile getStoredFile(String storedPath) {
        return storage.get(storedPath);
    }

    // Méthodes utilitaires privées
    private void simulateDelay() {
        if (operationDelay > 0) {
            try {
                Thread.sleep(operationDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            default -> "application/octet-stream";
        };
    }
}