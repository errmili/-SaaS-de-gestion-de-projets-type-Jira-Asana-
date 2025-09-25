package com.projectsaas.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "antivirus")
public class AntivirusProperties {
    private boolean enabled = false;
    private String engine = "mock";
    private String clamavHost = "localhost";
    private int clamavPort = 3310;
}