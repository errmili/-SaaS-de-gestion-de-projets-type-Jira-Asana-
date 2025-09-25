package com.projectsaas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityDto {
    private LocalDate date;
    private Integer tasksCompleted;
    private Integer hoursActive;
    private Integer projectsWorked;
    private Double productivityScore;
}