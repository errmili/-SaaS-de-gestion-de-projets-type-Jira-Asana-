// ===========================================
// DashboardDto.java - DTO tableau de bord
// ===========================================
package com.projectsaas.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    // Projets récents
    private List<ProjectDto> recentProjects;

    // Tâches assignées à l'utilisateur
    private List<TaskDto> myTasks;

    // Tâches en retard
    private List<TaskDto> overdueTasks;

    // Tâches récemment mises à jour
    private List<TaskDto> recentlyUpdatedTasks;

    // Sprints actifs
    private List<SprintDto> activeSprints;

    // Statistiques globales
    private DashboardStats stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private Integer totalProjects;
        private Integer totalTasks;
        private Integer myOpenTasks;
        private Integer overdueTasksCount;
        private Integer activeSprints;
        private Integer completedTasksThisWeek;
    }
}