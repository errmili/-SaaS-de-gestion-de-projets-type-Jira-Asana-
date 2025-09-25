package com.projectsaas.file.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {
    private UUID id;
    private UUID fileId;

    // Métadonnées EXIF pour images
    private String cameraMake;
    private String cameraModel;
    private Double gpsLatitude;
    private Double gpsLongitude;

    // Métadonnées documents
    private String documentTitle;
    private String documentAuthor;
    private String documentSubject;
    private Integer pageCount;

    // Thumbnails URLs
    private String thumbnailSmall;
    private String thumbnailMedium;
    private String thumbnailLarge;

    // Métadonnées personnalisées
    private Map<String, String> customMetadata;
}
