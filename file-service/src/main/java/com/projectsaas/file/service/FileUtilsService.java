package com.projectsaas.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileUtilsService {

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );

    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    );

    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "flac", "aac", "ogg", "wma"
    );

    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md"
    );

    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    public boolean isImage(String filename) {
        String extension = getFileExtension(filename);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    public boolean isVideo(String filename) {
        String extension = getFileExtension(filename);
        return VIDEO_EXTENSIONS.contains(extension);
    }

    public boolean isAudio(String filename) {
        String extension = getFileExtension(filename);
        return AUDIO_EXTENSIONS.contains(extension);
    }

    public boolean isDocument(String filename) {
        String extension = getFileExtension(filename);
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    public String getFileCategory(String filename) {
        if (isImage(filename)) return "image";
        if (isVideo(filename)) return "video";
        if (isAudio(filename)) return "audio";
        if (isDocument(filename)) return "document";
        return "other";
    }

    public String formatFileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        double size = bytes / Math.pow(1024, unitIndex);

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(size) + " " + units[unitIndex];
    }

    public long parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 0;
        }

        sizeStr = sizeStr.trim().toUpperCase();
        long multiplier = 1;

        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024L * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("TB")) {
            multiplier = 1024L * 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        }

        try {
            return (long) (Double.parseDouble(sizeStr.trim()) * multiplier);
        } catch (NumberFormatException e) {
            log.error("Invalid size format: {}", sizeStr);
            return 0;
        }
    }

    public String sanitizeFilename(String filename) {
        if (filename == null) return null;

        // Remplacer les caractères dangereux
        return filename.replaceAll("[^a-zA-Z0-9._\\-]", "_")
                .replaceAll("_{2,}", "_")
                .trim();
    }

    public boolean isExecutableFile(String filename) {
        String extension = getFileExtension(filename);
        List<String> executableExtensions = Arrays.asList(
                "exe", "bat", "cmd", "com", "scr", "vbs", "js", "jar",
                "msi", "dll", "sh", "php", "asp", "jsp"
        );
        return executableExtensions.contains(extension);
    }
}