package com.projectsaas.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "local";
    private Local local = new Local();
    private S3 s3 = new S3();
    private Minio minio = new Minio();

    @Data
    public static class Local {
        private String basePath = "/var/files";
        private String maxSize = "10GB";
    }

    @Data
    public static class S3 {
        private String bucketName;
        private String accessKey;
        private String secretKey;
        private String region = "us-east-1";
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }
}