package com.projectsaas.analytics.util;


import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetricsCalculator {

    public static double calculatePercentage(long part, long total) {
        return total > 0 ? (double) part / total * 100 : 0.0;
    }

    public static double calculateAverage(List<Double> values) {
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static double calculateTrend(double current, double previous) {
        return previous > 0 ? ((current - previous) / previous) * 100 : 0.0;
    }

    public static double calculateProductivityScore(int tasksCompleted, int tasksOverdue, double onTimeRate) {
        double baseScore = Math.min(tasksCompleted * 5, 100); // Max 100 from tasks
        double onTimeBonus = onTimeRate * 0.5; // Bonus for on-time delivery
        double overduePenalty = tasksOverdue * 10; // Penalty for overdue

        return Math.max(0, Math.min(100, baseScore + onTimeBonus - overduePenalty));
    }
}