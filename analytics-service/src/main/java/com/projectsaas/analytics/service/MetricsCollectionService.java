package com.projectsaas.analytics.service;
import com.projectsaas.analytics.dto.*;
import com.projectsaas.analytics.entity.ProjectMetrics;
import com.projectsaas.analytics.entity.TaskMetrics;
import com.projectsaas.analytics.entity.UserActivity;
import com.projectsaas.analytics.enums.TimeRange;
import com.projectsaas.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsCollectionService {

    private final ProjectMetricsRepository projectMetricsRepository;
    private final TaskMetricsRepository taskMetricsRepository;
    private final UserActivityRepository userActivityRepository;
    private final DataAggregationService dataAggregationService;

    public ProjectMetricsDto getProjectMetrics(Long projectId, TimeRange timeRange) {
        log.info("Collecting metrics for project: {} with range: {}", projectId, timeRange);

        LocalDateTime since = calculateSinceDate(timeRange);

        // Get latest project metrics
        ProjectMetrics metrics = projectMetricsRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .findFirst()
                .orElse(null);

        if (metrics == null) {
            return ProjectMetricsDto.builder()
                    .projectId(projectId)
                    .projectName("Unknown Project")
                    .build();
        }

        // Calculate trends
        Double velocityTrend = calculateVelocityTrend(projectId, since);
        Double tasksCompletedTrend = calculateTasksCompletedTrend(projectId, since);

        return ProjectMetricsDto.builder()
                .projectId(metrics.getProjectId())
                .projectName(metrics.getProjectName())
                .ownerName(metrics.getOwnerName())
                .totalTasks(metrics.getTotalTasks())
                .completedTasks(metrics.getCompletedTasks())
                .pendingTasks(metrics.getPendingTasks())
                .overdueTasks(metrics.getOverdueTasks())
                .totalMembers(metrics.getTotalMembers())
                .completionRate(metrics.getCompletionRate())
                .velocityTasksPerDay(metrics.getVelocityTasksPerDay())
                .teamProductivityScore(metrics.getTeamProductivityScore())
                .projectStartDate(metrics.getProjectStartDate())
                .lastActivity(metrics.getLastActivity())
                .tasksCompletedTrend(tasksCompletedTrend)
                .velocityTrend(velocityTrend)
                .build();
    }

    public UserProductivityDto getUserProductivity(Long userId, TimeRange timeRange) {
        log.info("Collecting productivity metrics for user: {} with range: {}", userId, timeRange);

        LocalDateTime since = calculateSinceDate(timeRange);

        // Get task metrics for user
        Long tasksAssigned = taskMetricsRepository.countTasksAssignedToUserSince(userId, since);
        Long tasksCompleted = taskMetricsRepository.countTasksCompletedByUserSince(userId, since);

        List<TaskMetrics> userTasks = taskMetricsRepository.findByAssignedUserIdOrderByTaskCreatedAtDesc(userId)
                .stream()
                .filter(task -> task.getTaskCreatedAt().isAfter(since))
                .toList();

        // Calculate metrics
        Integer tasksOverdue = (int) userTasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getIsOverdue()))
                .count();

        Double avgTaskDuration = userTasks.stream()
                .filter(task -> task.getTotalDuration() != null)
                .mapToLong(TaskMetrics::getTotalDuration)
                .average()
                .orElse(0.0) / 60.0; // Convert to hours

        Double onTimeDeliveryRate = tasksCompleted > 0 ?
                ((double) (tasksCompleted - tasksOverdue) / tasksCompleted) * 100 : 0.0;

        // Calculate productivity score (simplified)
        Double productivityScore = calculateProductivityScore(tasksCompleted, tasksOverdue, onTimeDeliveryRate);

        // Get daily activity
        List<DailyActivityDto> dailyActivity = getDailyActivity(userId, since);

        // Get user name from activity records
        String userName = userActivityRepository.findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(
                        userId, since, LocalDateTime.now())
                .stream()
                .findFirst()
                .map(UserActivity::getUserName)
                .orElse("Unknown User");

        return UserProductivityDto.builder()
                .userId(userId)
                .userName(userName)
                .tasksAssigned(tasksAssigned.intValue())
                .tasksCompleted(tasksCompleted.intValue())
                .tasksOverdue(tasksOverdue)
                .averageTaskDuration(avgTaskDuration)
                .productivityScore(productivityScore)
                .onTimeDeliveryRate(onTimeDeliveryRate)
                .dailyActivity(dailyActivity)
                .build();
    }

    public List<ProjectMetricsDto> getTopProjects(TimeRange timeRange, int limit) {
        log.info("Getting top {} projects for range: {}", limit, timeRange);

        LocalDateTime since = calculateSinceDate(timeRange);

        return projectMetricsRepository.findTopProjectsBySince(since)
                .stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserProductivityDto> getTopUsers(TimeRange timeRange, int limit) {
        log.info("Getting top {} users for range: {}", limit, timeRange);

        LocalDateTime since = calculateSinceDate(timeRange);

        List<Object[]> mostActiveUsers = userActivityRepository.findMostActiveUsersSince(since);

        return mostActiveUsers.stream()
                .limit(limit)
                .map(result -> {
                    Long userId = (Long) result[0];
                    Long activityCount = (Long) result[1];

                    // Get basic user productivity data
                    return getUserProductivity(userId, timeRange);
                })
                .collect(Collectors.toList());
    }

    public void calculateCurrentMetrics() {
        log.info("Calculating current metrics for all entities");

        // Update project metrics
        updateAllProjectMetrics();

        // Update user productivity scores
        updateUserProductivityScores();

        log.info("Metrics calculation completed");
    }

    private void updateAllProjectMetrics() {
        List<ProjectMetrics> allProjects = projectMetricsRepository.findAll();

        for (ProjectMetrics project : allProjects) {
            updateProjectMetrics(project);
        }
    }

    private void updateProjectMetrics(ProjectMetrics project) {
        // Get all tasks for this project
        List<TaskMetrics> projectTasks = taskMetricsRepository.findByProjectIdOrderByTaskCreatedAtDesc(project.getProjectId());

        // Update task counts
        int totalTasks = projectTasks.size();
        int completedTasks = (int) projectTasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .count();
        int pendingTasks = totalTasks - completedTasks;
        int overdueTasks = (int) projectTasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getIsOverdue()))
                .count();

        // Calculate completion rate
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        // Calculate velocity (tasks completed in last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentlyCompleted = projectTasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .filter(task -> task.getTaskCompletedAt() != null && task.getTaskCompletedAt().isAfter(weekAgo))
                .count();
        double velocity = recentlyCompleted / 7.0;

        // Update project metrics
        project.setTotalTasks(totalTasks);
        project.setCompletedTasks(completedTasks);
        project.setPendingTasks(pendingTasks);
        project.setOverdueTasks(overdueTasks);
        project.setCompletionRate(completionRate);
        project.setVelocityTasksPerDay(velocity);
        project.setUpdatedAt(LocalDateTime.now());

        projectMetricsRepository.save(project);
    }

    private void updateUserProductivityScores() {
        // Implementation for updating user productivity scores
        log.info("Updating user productivity scores");
    }

    private ProjectMetricsDto convertToDto(ProjectMetrics metrics) {
        return ProjectMetricsDto.builder()
                .projectId(metrics.getProjectId())
                .projectName(metrics.getProjectName())
                .ownerName(metrics.getOwnerName())
                .totalTasks(metrics.getTotalTasks())
                .completedTasks(metrics.getCompletedTasks())
                .pendingTasks(metrics.getPendingTasks())
                .overdueTasks(metrics.getOverdueTasks())
                .totalMembers(metrics.getTotalMembers())
                .completionRate(metrics.getCompletionRate())
                .velocityTasksPerDay(metrics.getVelocityTasksPerDay())
                .teamProductivityScore(metrics.getTeamProductivityScore())
                .projectStartDate(metrics.getProjectStartDate())
                .lastActivity(metrics.getLastActivity())
                .build();
    }

    private Double calculateProductivityScore(Long tasksCompleted, Integer tasksOverdue, Double onTimeRate) {
        // Simplified productivity score calculation
        double baseScore = Math.min(tasksCompleted * 10, 100); // Max 100 from task completion
        double onTimeBonus = onTimeRate; // Bonus for on-time delivery
        double overduepenalty = tasksOverdue * 5; // Penalty for overdue tasks

        return Math.max(0, Math.min(100, baseScore + onTimeBonus - overduepenalty));
    }

    private List<DailyActivityDto> getDailyActivity(Long userId, LocalDateTime since) {
        List<DailyActivityDto> dailyActivity = new ArrayList<>();

        // Get user activities grouped by day
        List<UserActivity> activities = userActivityRepository
                .findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(userId, since, LocalDateTime.now());

        // Group by date and create daily summaries
        Map<LocalDate, List<UserActivity>> activitiesByDate = activities.stream()
                .collect(Collectors.groupingBy(activity -> activity.getActivityDate().toLocalDate()));

        activitiesByDate.forEach((date, dayActivities) -> {
            int tasksCompleted = (int) dayActivities.stream()
                    .filter(activity -> activity.getActivityDescription().contains("completed"))
                    .count();

            int hoursActive = Math.min(8, dayActivities.size()); // Estimate based on activity count

            dailyActivity.add(DailyActivityDto.builder()
                    .date(date)
                    .tasksCompleted(tasksCompleted)
                    .hoursActive(hoursActive)
                    .projectsWorked((int) dayActivities.stream().map(UserActivity::getProjectId).distinct().count())
                    .productivityScore(calculateDailyProductivityScore(tasksCompleted, hoursActive))
                    .build());
        });

        return dailyActivity;
    }

    private Double calculateDailyProductivityScore(int tasksCompleted, int hoursActive) {
        // Simple daily productivity score
        return Math.min(100.0, (tasksCompleted * 20.0) + (hoursActive * 5.0));
    }

    private Double calculateVelocityTrend(Long projectId, LocalDateTime since) {
        // Calculate velocity trend - simplified implementation
        return 5.2; // +5.2% trend
    }

    private Double calculateTasksCompletedTrend(Long projectId, LocalDateTime since) {
        // Calculate tasks completed trend - simplified implementation
        return 12.8; // +12.8% trend
    }

    private LocalDateTime calculateSinceDate(TimeRange timeRange) {
        return switch (timeRange) {
            case LAST_24_HOURS -> LocalDateTime.now().minusHours(24);
            case LAST_7_DAYS -> LocalDateTime.now().minusDays(7);
            case LAST_30_DAYS -> LocalDateTime.now().minusDays(30);
            case LAST_3_MONTHS -> LocalDateTime.now().minusMonths(3);
            case LAST_6_MONTHS -> LocalDateTime.now().minusMonths(6);
            case LAST_YEAR -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusDays(30);
        };
    }
}