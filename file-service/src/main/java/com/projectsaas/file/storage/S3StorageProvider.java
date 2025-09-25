package com.projectsaas.file.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component("s3")
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@Slf4j
public class S3StorageProvider implements StorageProvider {

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Value("${storage.s3.access-key}")
    private String accessKey;

    @Value("${storage.s3.secret-key}")
    private String secretKey;

    @Value("${storage.s3.region:us-east-1}")
    private String region;

    @Value("${storage.s3.endpoint:}")
    private String endpoint;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        try {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

            var s3ClientBuilder = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

            // Support pour endpoint personnalisé (compatible S3)
            if (endpoint != null && !endpoint.trim().isEmpty()) {
                s3ClientBuilder.endpointOverride(java.net.URI.create(endpoint));
            }

            this.s3Client = s3ClientBuilder.build();

            var presignerBuilder = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

            if (endpoint != null && !endpoint.trim().isEmpty()) {
                presignerBuilder.endpointOverride(java.net.URI.create(endpoint));
            }

            this.s3Presigner = presignerBuilder.build();

            // Vérifier/créer le bucket
            ensureBucketExists();

            log.info("S3StorageProvider initialized with bucket: {}", bucketName);

        } catch (Exception e) {
            log.error("Failed to initialize S3 storage", e);
            throw new StorageException("s3", "init", "Failed to initialize S3 storage", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName, String tenantId) {
        try {
            // Créer clé avec structure organisée
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String extension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() +
                    (extension.isEmpty() ? "" : "." + extension);

            String key = tenantId + "/" + datePath + "/" + uniqueFileName;

            // Préparer les métadonnées
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(getContentType(fileName))
                    .metadata(java.util.Map.of(
                            "original-name", fileName,
                            "tenant-id", tenantId,
                            "upload-date", LocalDateTime.now().toString()
                    ))
                    .build();

            // Upload vers S3
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

            log.debug("File stored in S3: {}", key);
            return key;

        } catch (Exception e) {
            log.error("Failed to store file in S3: {}", fileName, e);
            throw new StorageException("s3", "store", "Failed to store file in S3: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String storedPath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedPath)
                    .build();

            return s3Client.getObject(getRequest);

        } catch (NoSuchKeyException e) {
            throw new StorageException("s3", "retrieve", "File not found: " + storedPath);
        } catch (Exception e) {
            log.error("Failed to retrieve file from S3: {}", storedPath, e);
            throw new StorageException("s3", "retrieve", "Failed to retrieve file from S3: " + storedPath, e);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedPath)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.debug("File deleted from S3: {}", storedPath);

        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", storedPath, e);
            throw new StorageException("s3", "delete", "Failed to delete file from S3: " + storedPath, e);
        }
    }

    @Override
    public String generatePresignedUrl(String storedPath, Duration expiration) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedPath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", storedPath, e);
            throw new StorageException("s3", "presign", "Failed to generate presigned URL: " + storedPath, e);
        }
    }

    @Override
    public boolean exists(String storedPath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedPath)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if file exists: {}", storedPath, e);
            return false;
        }
    }

    @Override
    public long getFileSize(String storedPath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedPath)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();

        } catch (Exception e) {
            log.error("Failed to get file size from S3: {}", storedPath, e);
            return 0;
        }
    }

    @Override
    public void copy(String sourcePath, String destPath) {
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourcePath)
                    .destinationBucket(bucketName)
                    .destinationKey(destPath)
                    .build();

            s3Client.copyObject(copyRequest);
            log.debug("File copied in S3 from {} to {}", sourcePath, destPath);

        } catch (Exception e) {
            log.error("Failed to copy file in S3 from {} to {}", sourcePath, destPath, e);
            throw new StorageException("s3", "copy",
                    String.format("Failed to copy file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public void move(String sourcePath, String destPath) {
        try {
            // S3 ne supporte pas le move natif, on fait copy + delete
            copy(sourcePath, destPath);
            delete(sourcePath);
            log.debug("File moved in S3 from {} to {}", sourcePath, destPath);

        } catch (Exception e) {
            log.error("Failed to move file in S3 from {} to {}", sourcePath, destPath, e);
            throw new StorageException("s3", "move",
                    String.format("Failed to move file from %s to %s", sourcePath, destPath), e);
        }
    }

    @Override
    public StorageStats getStats() {
        try {
            // Note: S3 ne fournit pas facilement les stats globales
            // En production, utiliser CloudWatch ou calculer périodiquement

            // Pour une approximation, on peut lister les objets (limité à 1000 par défaut)
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .maxKeys(1000)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

            long totalFiles = response.keyCount();
            long totalSize = response.contents().stream()
                    .mapToLong(S3Object::size)
                    .sum();

            StorageStats stats = StorageStats.builder()
                    .provider("S3")
                    .totalFiles(totalFiles)
                    .totalSize(totalSize)
                    .usedSpace(totalSize)
                    .availableSpace(Long.MAX_VALUE) // S3 = "illimité"
                    .healthy(true)
                    .statusMessage("S3 storage operational")
                    .build();

            stats.calculateUsagePercentage();
            stats.formatSizes();

            return stats;

        } catch (Exception e) {
            log.error("Failed to get S3 storage stats", e);
            return StorageStats.builder()
                    .provider("S3")
                    .healthy(false)
                    .statusMessage("Error getting S3 stats: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Test simple: vérifier l'accès au bucket
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            return true;

        } catch (Exception e) {
            log.error("S3 health check failed", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "S3";
    }

    // Méthodes utilitaires privées
    private void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            log.debug("S3 bucket exists: {}", bucketName);

        } catch (NoSuchBucketException e) {
            // Créer le bucket s'il n'existe pas
            try {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();

                s3Client.createBucket(createBucketRequest);
                log.info("Created S3 bucket: {}", bucketName);

            } catch (Exception createException) {
                log.error("Failed to create S3 bucket: {}", bucketName, createException);
                throw new StorageException("s3", "createBucket",
                        "Failed to create bucket: " + bucketName, createException);
            }
        } catch (Exception e) {
            log.error("Failed to check S3 bucket: {}", bucketName, e);
            throw new StorageException("s3", "headBucket",
                    "Failed to access bucket: " + bucketName, e);
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
