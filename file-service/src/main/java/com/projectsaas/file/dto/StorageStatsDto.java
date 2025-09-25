package com.projectsaas.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStatsDto {
    private Integer totalFiles;
    private Long totalSize;
    private String humanReadableTotalSize;
    private Map<String, TypeStats> typeStats;
    private String storageProvider;
    private Long availableSpace;
    private Long usedSpace;
    private Double usagePercentage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private Integer count;
        private Long size;
        private String humanReadableSize;
        private Double percentage;
    }
}