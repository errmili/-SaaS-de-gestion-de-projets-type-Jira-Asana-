package com.projectsaas.analytics.kafka.listener;

import com.projectsaas.analytics.entity.UserActivity;
import com.projectsaas.analytics.enums.MetricType;
import com.projectsaas.analytics.repository.UserActivityRepository;
import com.projectsaas.analytics.repository.ProjectMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileAnalyticsListener {

    private final UserActivityRepository userActivityRepository;
    private final ProjectMetricsRepository projectMetricsRepository;

    @KafkaListener(topics = "file.uploaded", groupId = "analytics-service")
    public void handleFileUploaded(Map<String, Object> event) {
        log.info("Received file uploaded event: {}", event);

        try {
            Long userId = ((Number) event.get("userId")).longValue();
            String userName = (String) event.get("userName");
            String fileName = (String) event.get("fileName");
            Long fileSize = ((Number) event.get("fileSize")).longValue();
            String fileType = (String) event.get("fileType");
            Long projectId = event.get("projectId") != null ? ((Number) event.get("projectId")).longValue() : null;
            String projectName = (String) event.get("projectName");

            // Record user activity
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .userName(userName)
                    .activityType(MetricType.FILE_UPLOADED)
                    .activityDescription("Uploaded file: " + fileName)
                    .projectId(projectId)
                    .projectName(projectName)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"fileName\":\"%s\",\"fileSize\":%d,\"fileType\":\"%s\"}",
                            fileName, fileSize, fileType))
                    .build();

            userActivityRepository.save(activity);

            // Update project metrics if file is linked to a project
            if (projectId != null) {
                updateProjectFileMetrics(projectId, fileSize);
            }

        } catch (Exception e) {
            log.error("Error processing file uploaded event", e);
        }
    }

    @KafkaListener(topics = "file.downloaded", groupId = "analytics-service")
    public void handleFileDownloaded(Map<String, Object> event) {
        log.info("Received file downloaded event: {}", event);

        try {
            Long userId = ((Number) event.get("userId")).longValue();
            String userName = (String) event.get("userName");
            String fileName = (String) event.get("fileName");
            Long projectId = event.get("projectId") != null ? ((Number) event.get("projectId")).longValue() : null;
            String projectName = (String) event.get("projectName");

            // Record user activity
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .userName(userName)
                    .activityType(MetricType.USER_ACTIVITY)
                    .activityDescription("Downloaded file: " + fileName)
                    .projectId(projectId)
                    .projectName(projectName)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"fileName\":\"%s\",\"action\":\"download\"}", fileName))
                    .build();

            userActivityRepository.save(activity);

        } catch (Exception e) {
            log.error("Error processing file downloaded event", e);
        }
    }

    @KafkaListener(topics = "file.deleted", groupId = "analytics-service")
    public void handleFileDeleted(Map<String, Object> event) {
        log.info("Received file deleted event: {}", event);

        try {
            Long userId = ((Number) event.get("userId")).longValue();
            String userName = (String) event.get("userName");
            String fileName = (String) event.get("fileName");
            Long fileSize = ((Number) event.get("fileSize")).longValue();
            Long projectId = event.get("projectId") != null ? ((Number) event.get("projectId")).longValue() : null;
            String projectName = (String) event.get("projectName");

            // Record user activity
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .userName(userName)
                    .activityType(MetricType.USER_ACTIVITY)
                    .activityDescription("Deleted file: " + fileName)
                    .projectId(projectId)
                    .projectName(projectName)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"fileName\":\"%s\",\"action\":\"delete\"}", fileName))
                    .build();

            userActivityRepository.save(activity);

            // Update project metrics if file was linked to a project
            if (projectId != null) {
                updateProjectFileMetrics(projectId, -fileSize); // Negative to subtract
            }

        } catch (Exception e) {
            log.error("Error processing file deleted event", e);
        }
    }

    private void updateProjectFileMetrics(Long projectId, Long fileSizeDelta) {
        projectMetricsRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .findFirst()
                .ifPresent(metrics -> {
                    // Update file count and size
                    if (fileSizeDelta > 0) {
                        metrics.setTotalFilesUploaded(metrics.getTotalFilesUploaded() + 1);
                    } else {
                        metrics.setTotalFilesUploaded(Math.max(0, metrics.getTotalFilesUploaded() - 1));
                    }

                    long newSize = metrics.getTotalFilesSizeMB() + (fileSizeDelta / (1024 * 1024)); // Convert to MB
                    metrics.setTotalFilesSizeMB(Math.max(0, newSize));

                    metrics.setLastActivity(LocalDateTime.now());
                    metrics.setUpdatedAt(LocalDateTime.now());
                    projectMetricsRepository.save(metrics);
                });
    }
}