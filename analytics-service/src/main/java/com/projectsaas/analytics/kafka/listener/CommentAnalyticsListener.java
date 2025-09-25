package com.projectsaas.analytics.kafka.listener;

import com.projectsaas.analytics.entity.UserActivity;
import com.projectsaas.analytics.enums.MetricType;
import com.projectsaas.analytics.repository.UserActivityRepository;
import com.projectsaas.analytics.repository.TaskMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentAnalyticsListener {

    private final UserActivityRepository userActivityRepository;
    private final TaskMetricsRepository taskMetricsRepository;

    @KafkaListener(topics = "comment.added", groupId = "analytics-service")
    public void handleCommentAdded(Map<String, Object> event) {
        log.info("Received comment added event: {}", event);

        try {
            Long userId = ((Number) event.get("userId")).longValue();
            String userName = (String) event.get("userName");
            Long taskId = ((Number) event.get("taskId")).longValue();
            String taskTitle = (String) event.get("taskTitle");
            Long projectId = ((Number) event.get("projectId")).longValue();
            String projectName = (String) event.get("projectName");
            String commentText = (String) event.get("commentText");

            // Record user activity
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .userName(userName)
                    .activityType(MetricType.USER_ACTIVITY)
                    .activityDescription("Added comment on task: " + taskTitle)
                    .projectId(projectId)
                    .projectName(projectName)
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"action\":\"comment\",\"commentLength\":%d}",
                            commentText.length()))
                    .build();

            userActivityRepository.save(activity);

            // Update task metrics - increment comment count
            taskMetricsRepository.findByTaskId(taskId)
                    .stream()
                    .findFirst()
                    .ifPresent(taskMetrics -> {
                        taskMetrics.setCommentsCount(taskMetrics.getCommentsCount() + 1);
                        taskMetrics.setUpdatedAt(LocalDateTime.now());
                        taskMetricsRepository.save(taskMetrics);
                    });

        } catch (Exception e) {
            log.error("Error processing comment added event", e);
        }
    }

    @KafkaListener(topics = "mention.created", groupId = "analytics-service")
    public void handleMentionCreated(Map<String, Object> event) {
        log.info("Received mention created event: {}", event);

        try {
            Long mentionedUserId = ((Number) event.get("mentionedUserId")).longValue();
            String mentionedUserName = (String) event.get("mentionedUserName");
            Long mentionByUserId = ((Number) event.get("mentionByUserId")).longValue();
            String mentionByUserName = (String) event.get("mentionByUserName");
            Long taskId = event.get("taskId") != null ? ((Number) event.get("taskId")).longValue() : null;
            String taskTitle = (String) event.get("taskTitle");
            Long projectId = ((Number) event.get("projectId")).longValue();
            String projectName = (String) event.get("projectName");

            // Record activity for the user who was mentioned
            UserActivity mentionedActivity = UserActivity.builder()
                    .userId(mentionedUserId)
                    .userName(mentionedUserName)
                    .activityType(MetricType.USER_ACTIVITY)
                    .activityDescription("Mentioned by " + mentionByUserName + " in " +
                            (taskId != null ? "task: " + taskTitle : "project: " + projectName))
                    .projectId(projectId)
                    .projectName(projectName)
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"action\":\"mentioned_by\",\"mentionByUserId\":%d}", mentionByUserId))
                    .build();

            userActivityRepository.save(mentionedActivity);

            // Record activity for the user who made the mention
            UserActivity mentionByActivity = UserActivity.builder()
                    .userId(mentionByUserId)
                    .userName(mentionByUserName)
                    .activityType(MetricType.USER_ACTIVITY)
                    .activityDescription("Mentioned " + mentionedUserName + " in " +
                            (taskId != null ? "task: " + taskTitle : "project: " + projectName))
                    .projectId(projectId)
                    .projectName(projectName)
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .activityDate(LocalDateTime.now())
                    .metadata(String.format("{\"action\":\"mentioned_user\",\"mentionedUserId\":%d}", mentionedUserId))
                    .build();

            userActivityRepository.save(mentionByActivity);

        } catch (Exception e) {
            log.error("Error processing mention created event", e);
        }
    }
}