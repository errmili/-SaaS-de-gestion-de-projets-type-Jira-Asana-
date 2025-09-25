package com.projectsaas.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
public class ThumbnailService {

    public enum ThumbnailSize {
        SMALL(150, 150),
        MEDIUM(300, 300),
        LARGE(600, 600);

        public final int width;
        public final int height;

        ThumbnailSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public Map<ThumbnailSize, String> generateThumbnails(InputStream imageStream,
                                                         String originalPath,
                                                         String tenantId) {
        // Mock implementation
        log.debug("Mock thumbnail generation for: {}", originalPath);
        return Map.of(
                ThumbnailSize.SMALL, originalPath + "_small.jpg",
                ThumbnailSize.MEDIUM, originalPath + "_medium.jpg",
                ThumbnailSize.LARGE, originalPath + "_large.jpg"
        );
    }
}