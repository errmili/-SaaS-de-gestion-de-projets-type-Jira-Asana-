package com.projectsaas.file.storage;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Statistiques de stockage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStats {

    /** Nombre total de fichiers */
    private long totalFiles;

    /** Taille totale utilisée en bytes */
    private long totalSize;

    /** Espace disponible en bytes */
    private long availableSpace;

    /** Espace utilisé en bytes */
    private long usedSpace;

    /** Pourcentage d'utilisation */
    private double usagePercentage;

    /** Nom du provider */
    private String provider;

    /** Taille formatée lisible */
    private String humanReadableSize;

    /** Espace disponible formaté */
    private String humanReadableAvailable;

    /** Santé du stockage */
    private boolean healthy;

    /** Message d'état */
    private String statusMessage;

    /**
     * Calculer le pourcentage d'utilisation
     */
    public void calculateUsagePercentage() {
        if (totalSize > 0 && (usedSpace + availableSpace) > 0) {
            long totalSpace = usedSpace + availableSpace;
            this.usagePercentage = (double) usedSpace / totalSpace * 100.0;
        } else {
            this.usagePercentage = 0.0;
        }
    }

    /**
     * Formatter la taille en format lisible
     */
    public void formatSizes() {
        this.humanReadableSize = formatBytes(this.totalSize);
        this.humanReadableAvailable = formatBytes(this.availableSpace);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}