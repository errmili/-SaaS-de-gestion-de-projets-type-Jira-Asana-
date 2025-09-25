package com.projectsaas.file.service;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ScanResult {
    private ScanStatus status;
    private String message;
    private String details;

    public enum ScanStatus {
        CLEAN, INFECTED, ERROR, PENDING, SKIPPED
    }

    public static ScanResult clean(String message) {
        return new ScanResult(ScanStatus.CLEAN, message, null);
    }

    public static ScanResult infected(String message) {
        return new ScanResult(ScanStatus.INFECTED, message, null);
    }

    public static ScanResult error(String message) {
        return new ScanResult(ScanStatus.ERROR, message, null);
    }

    public static ScanResult pending() {
        return new ScanResult(ScanStatus.PENDING, "Scan in progress", null);
    }

    public static ScanResult skipped(String reason) {
        return new ScanResult(ScanStatus.SKIPPED, reason, null);
    }

    public boolean isClean() {
        return status == ScanStatus.CLEAN;
    }

    public boolean isInfected() {
        return status == ScanStatus.INFECTED;
    }

    public boolean hasError() {
        return status == ScanStatus.ERROR;
    }
}