package com.projectsaas.file.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    // Métadonnées EXIF pour images
    @Column(name = "camera_make")
    private String cameraMake;

    @Column(name = "camera_model")
    private String cameraModel;

    @Column(name = "gps_latitude")
    private Double gpsLatitude;

    @Column(name = "gps_longitude")
    private Double gpsLongitude;

    // Métadonnées documents
    @Column(name = "document_title")
    private String documentTitle;

    @Column(name = "document_author")
    private String documentAuthor;

    @Column(name = "document_subject")
    private String documentSubject;

    @Column(name = "page_count")
    private Integer pageCount;

    // Métadonnées personnalisées (JSON)
    @ElementCollection
    @CollectionTable(name = "file_custom_metadata")
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> customMetadata;

    // Thumbnails paths
    @Column(name = "thumbnail_small")
    private String thumbnailSmall;   // 150x150

    @Column(name = "thumbnail_medium")
    private String thumbnailMedium;  // 300x300

    @Column(name = "thumbnail_large")
    private String thumbnailLarge;   // 600x600
}