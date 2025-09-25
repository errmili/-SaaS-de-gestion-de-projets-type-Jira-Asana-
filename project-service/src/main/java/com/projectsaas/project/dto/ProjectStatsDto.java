// ===========================================
// ProjectStatsDto.java - Statistiques de projet
// ===========================================
package com.projectsaas.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStatsDto {

    private UUID projectId;
    private String projectName;

    // Statistiques des tâches
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer todoTasks;
    private Integer blockedTasks;

    // Statistiques par priorité
    private Map<String, Integer> tasksByPriority;

    // Statistiques par type
    private Map<String, Integer> tasksByType;

    // Statistiques des sprints
    private Integer totalSprints;
    private Integer activeSprints;
    private Integer completedSprints;

    // Statistiques des membres
    private Integer totalMembers;

    // Métriques de performance
    private Double completionRate; // Pourcentage de tâches terminées
    private Integer averageTasksPerSprint;
    private Double averageStoryPointsPerTask;
}