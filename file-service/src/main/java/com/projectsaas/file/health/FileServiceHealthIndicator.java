package com.projectsaas.file.health;


import com.projectsaas.file.storage.StorageStats;
import lombok.RequiredArgsConstructor;
//import org.springframework.boot.actuator.health.Health;
//import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileServiceHealthIndicator {//implements HealthIndicator {

//    private final StorageService storageService;
//
//    @Override
//    public Health health() {
//        try {
//            StorageStats stats = storageService.getStorageStats();
//
//            Health.Builder builder = Health.up()
//                    .withDetail("storage.provider", stats.getProvider())
//                    .withDetail("storage.totalFiles", stats.getTotalFiles())
//                    .withDetail("storage.totalSize", stats.getTotalSize())
//                    .withDetail("storage.usagePercentage", stats.getUsagePercentage());
//
//            // Avertissement si utilisation > 90%
//            if (stats.getUsagePercentage() > 90) {
//                builder.down().withDetail("warning", "Storage usage is high");
//            }
//
//            return builder.build();
//
//        } catch (Exception e) {
//            return Health.down()
//                    .withDetail("error", e.getMessage())
//                    .build();
//        }
//    }
}