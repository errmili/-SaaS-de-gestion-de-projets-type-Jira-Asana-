package com.projectsaas.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    private String maxSize = "100MB";
    private List<String> allowedExtensions;
    private List<String> blockedExtensions;
}
