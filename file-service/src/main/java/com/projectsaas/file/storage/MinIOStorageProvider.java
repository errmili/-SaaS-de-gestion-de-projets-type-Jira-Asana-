package com.projectsaas.file.storage;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("minio")
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
@Slf4j
public class MinIOStorageProvider implements StorageProvider {

    @Value("${storage.minio.endpoint}")
    private String endpoint;

    @Value("${storage.minio.access-key}")
    private String accessKey;

    @Value("${storage.minio.secret-key}")
    private String secretKey;

    @Value("${storage.minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Créer le bucket s'il n'existe pas
            ensureBucketExists();

            log.info("MinIOStorageProvider initialized with endpoint: {} bucket: {}", endpoint, bucketName);

        } catch (Exception e) {
            log.error("Failed to initialize MinIO storage", e);
            throw new StorageException("minio", "init", "Failed to initialize MinIO storage", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.debug("MinIO storage provider cleaned up");
    }

    @Override
    public String store(InputStream inputStream, String fileName, String tenantId) {
        try {
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String extension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() +
                    (extension.isEmpty() ? "" : "." + extension);

            String objectName = tenantId + "/" + datePath + "/" + uniqueFileName;

            // Préparer les métadonnées
            Map<String, String> userMetadata = new HashMap<>();
            userMetadata.put("original-name", fileName);
            userMetadata.put("tenant-id", tenantId);
            userMetadata.put("upload-date", LocalDateTime.now().toString());

            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(getContentType(fileName))
                    .userMetadata(userMetadata)
                    .build();

            minioClient.putObject(putArgs);

            log.debug("File stored in MinIO: {}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("Failed to store file in MinIO: {}", fileName, e);
            throw new StorageException("minio", "store", "Failed to store file in MinIO: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String storedPath) {
        try {
            GetObjectArgs getArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storedPath)
                    .build();

            return minioClient.getObject(getArgs);

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new StorageException("minio", "retrieve", "File not found: " + storedPath);
            }
            throw new StorageException("minio", "retrieve", "Failed to retrieve file: " + storedPath, e);
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", storedPath, e);
            throw new StorageException("minio", "retrieve", "Failed to retrieve file from MinIO: " + storedPath, e);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            RemoveObjectArgs removeArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storedPath)
                    .build();

            minioClient.removeObject(removeArgs);
            log.debug("File deleted from MinIO: {}", storedPath);

        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", storedPath, e);
            throw new StorageException("minio", "delete", "Failed to delete file from MinIO: " + storedPath, e);
        }
    }

    @Override
    public String generatePresignedUrl(String storedPath, Duration expiration) {
        try {
            GetPresignedObjectUrlArgs presignArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(storedPath)
                    .expiry((int) expiration.getSeconds())
                    .build();

            return minioClient.getPresignedObjectUrl(presignArgs);

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", storedPath, e);
            throw new StorageException("minio", "presign", "Failed to generate presigned URL: " + storedPath, e);
        }
    }

    @Override
    public boolean exists(String storedPath) {
        try {
            StatObjectArgs statArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storedPath)
                    .build();

            minioClient.statObject(statArgs);
            return true;

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            log.error("Failed to check if file exists: {}", storedPath, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to check if file exists: {}", storedPath, e);
            return false;
        }
    }

    @Override
    public long getFileSize(String storedPath) {
        try {
            StatObjectArgs statArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storedPath)
                    .build();

            StatObjectResponse stat = minioClient.statObject(statArgs);
            return stat.size();

        } catch (Exception e) {
            log.error("Failed to get file size from MinIO: {}", storedPath, e);
            return 0;
        }
    }

    @Override
    public void copy(String sourcePath, String destPath) {
        try {
            CopySource source = CopySource.builder()
                    .bucket(bucketName)
                    .object(sourcePath)
                    .build();

            CopyObjectArgs copyArgs = CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(destPath)
                    .source(source)
                    .build();

            minioClient.copyObject(copyArgs);
            log.debug("File copied in MinIO from {} to {}", sourcePath, destPath);

        } catch (Exception e) {
            log.error("Failed to copy file in MinIO from {} to {}", sourcePath, destPath, e);
            throw new StorageException("minio", "copy",
                    String.format("Failed to copy file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public void move(String sourcePath, String destPath) {
        try {
            copy(sourcePath, destPath);
            delete(sourcePath);
            log.debug("File moved in MinIO from {} to {}", sourcePath, destPath);

        } catch (Exception e) {
            log.error("Failed to move file in MinIO from {} to {}", sourcePath, destPath, e);
            throw new StorageException("minio", "move",
                    String.format("Failed to move file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public StorageStats getStats() {
        try {
            ListObjectsArgs listArgs = ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .maxKeys(1000)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(listArgs);

            long totalFiles = 0;
            long totalSize = 0;

            for (Result<Item> result : results) {
                Item item = result.get();
                totalFiles++;
                totalSize += item.size();
            }

            StorageStats stats = StorageStats.builder()
                    .provider("MinIO")
                    .totalFiles(totalFiles)
                    .totalSize(totalSize)
                    .usedSpace(totalSize)
                    .availableSpace(Long.MAX_VALUE)
                    .healthy(true)
                    .statusMessage("MinIO storage operational")
                    .build();

            stats.calculateUsagePercentage();
            stats.formatSizes();

            return stats;

        } catch (Exception e) {
            log.error("Failed to get MinIO storage stats", e);
            return StorageStats.builder()
                    .provider("MinIO")
                    .healthy(false)
                    .statusMessage("Error getting MinIO stats: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            return minioClient.bucketExists(bucketExistsArgs);

        } catch (Exception e) {
            log.error("MinIO health check failed", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "MinIO";
    }

    private void ensureBucketExists() {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            boolean exists = minioClient.bucketExists(bucketExistsArgs);

            if (!exists) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();

                minioClient.makeBucket(makeBucketArgs);
                log.info("Created MinIO bucket: {}", bucketName);
            } else {
                log.debug("MinIO bucket exists: {}", bucketName);
            }

        } catch (Exception e) {
            log.error("Failed to ensure MinIO bucket exists: {}", bucketName, e);
            throw new StorageException("minio", "ensureBucket",
                    "Failed to ensure bucket exists: " + bucketName, e);
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
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "csv" -> "text/csv";
            default -> "application/octet-stream";
        };
    }
}