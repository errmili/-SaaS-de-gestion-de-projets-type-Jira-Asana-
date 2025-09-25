package com.projectsaas.analytics.dto;

import com.projectsaas.analytics.enums.ReportType;
import com.projectsaas.analytics.enums.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {

    private ReportType reportType;
    private TimeRange timeRange;

    // Custom date range
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Filters
    private List<Long> projectIds;
    private List<Long> userIds;
    private List<String> includeMetrics;

    // Output format
    private String format; // PDF, EXCEL, JSON
    private Boolean includeCharts;
    private Boolean sendByEmail;
    private String recipientEmail;

    // Additional parameters
    private Map<String, Object> parameters;
}