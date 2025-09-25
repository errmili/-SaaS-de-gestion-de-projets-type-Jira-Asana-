package com.projectsaas.analytics.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerator {

    private final Path reportsDirectory;
    private final ExcelExporter excelExporter;

    public String generateReport(String templateName, Map<String, Object> data, String format) {
        log.info("Generating {} report using template: {}", format, templateName);

        String fileName = generateFileName(templateName, format);
        Path outputPath = reportsDirectory.resolve(fileName);

        try {
            return switch (format.toUpperCase()) {
                case "PDF" -> generatePdfReport(templateName, data, outputPath);
                case "EXCEL" -> generateExcelReport(templateName, data, outputPath);
                case "JSON" -> generateJsonReport(data, outputPath);
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
        } catch (Exception e) {
            log.error("Failed to generate report", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }

    private String generatePdfReport(String templateName, Map<String, Object> data, Path outputPath) {
        // Simplified PDF generation - in real implementation, use iText or similar
        log.info("Generating PDF report: {}", outputPath);

        // PDF generation implementation would go here
        // For now, return the path as string
        return outputPath.toString();
    }

    private String generateExcelReport(String templateName, Map<String, Object> data, Path outputPath) {
        log.info("Generating Excel report: {}", outputPath);

        return excelExporter.generateExcelReport(templateName, data, outputPath);
    }

    private String generateJsonReport(Map<String, Object> data, Path outputPath) {
        log.info("Generating JSON report: {}", outputPath);

        // JSON generation implementation would go here
        return outputPath.toString();
    }

    private String generateFileName(String templateName, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s.%s", templateName, timestamp, uuid, format.toLowerCase());
    }
}
