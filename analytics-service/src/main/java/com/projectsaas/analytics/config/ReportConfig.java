package com.projectsaas.analytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ReportConfig {

    @Value("${analytics.reports.output-dir:./reports}")
    private String outputDir;

    @Bean
    public Path reportsDirectory() {
        try {
            Path path = Paths.get(outputDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reports directory", e);
        }
    }
}