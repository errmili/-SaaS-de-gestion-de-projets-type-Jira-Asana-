// ===========================================
// KanbanBoardDto.java - DTO tableau Kanban
// ===========================================
package com.projectsaas.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanBoardDto {

    private UUID projectId;
    private String projectName;
    private List<KanbanColumn> columns;
    private Map<String, Integer> statusCounts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KanbanColumn {
        private String id;
        private String name;
        private String status;
        private Integer wipLimit;
        private List<TaskDto> tasks;
        private Integer taskCount;
    }
}
