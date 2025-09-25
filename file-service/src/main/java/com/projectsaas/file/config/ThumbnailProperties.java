package com.projectsaas.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "thumbnail")
public class ThumbnailProperties {
    private boolean enabled = true;
    private float quality = 0.8f;
    private boolean generateAsync = true;
}