package com.projectsaas.analytics.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class ExcelExporter {

    public String generateExcelReport(String templateName, Map<String, Object> data, Path outputPath) {
        log.info("Generating Excel report: {}", outputPath);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, data);

            // Create additional sheets based on template
            switch (templateName) {
                case "project-summary" -> createProjectSummarySheets(workbook, data);
                case "user-productivity" -> createUserProductivitySheets(workbook, data);
                case "team-performance" -> createTeamPerformanceSheets(workbook, data);
                default -> log.warn("Unknown template: {}", templateName);
            }

            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(outputPath.toFile())) {
                workbook.write(outputStream);
            }

            log.info("Excel report generated successfully: {}", outputPath);
            return outputPath.toString();

        } catch (IOException e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private void createSummarySheet(Sheet sheet, Map<String, Object> data) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Analytics Report");
        headerRow.createCell(1).setCellValue("Generated: " + LocalDateTime.now());

        // Add summary data
        int rowNum = 2;
        Row summaryRow = sheet.createRow(rowNum++);
        summaryRow.createCell(0).setCellValue("Report Type");
        summaryRow.createCell(1).setCellValue("Analytics Summary");

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createProjectSummarySheets(Workbook workbook, Map<String, Object> data) {
        Sheet projectSheet = workbook.createSheet("Projects");

        // Create headers
        Row headerRow = projectSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Project ID");
        headerRow.createCell(1).setCellValue("Project Name");
        headerRow.createCell(2).setCellValue("Completion Rate");
        headerRow.createCell(3).setCellValue("Total Tasks");
        headerRow.createCell(4).setCellValue("Completed Tasks");

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            projectSheet.autoSizeColumn(i);
        }
    }

    private void createUserProductivitySheets(Workbook workbook, Map<String, Object> data) {
        Sheet userSheet = workbook.createSheet("User Productivity");

        // Create headers
        Row headerRow = userSheet.createRow(0);
        headerRow.createCell(0).setCellValue("User ID");
        headerRow.createCell(1).setCellValue("User Name");
        headerRow.createCell(2).setCellValue("Tasks Completed");
        headerRow.createCell(3).setCellValue("Productivity Score");
        headerRow.createCell(4).setCellValue("On-Time Rate");

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            userSheet.autoSizeColumn(i);
        }
    }

    private void createTeamPerformanceSheets(Workbook workbook, Map<String, Object> data) {
        createProjectSummarySheets(workbook, data);
        createUserProductivitySheets(workbook, data);
    }
}