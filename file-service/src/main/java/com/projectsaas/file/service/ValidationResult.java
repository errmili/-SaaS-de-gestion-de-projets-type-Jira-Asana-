package com.projectsaas.file.service;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {
    private boolean valid = true;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
        valid = false;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
