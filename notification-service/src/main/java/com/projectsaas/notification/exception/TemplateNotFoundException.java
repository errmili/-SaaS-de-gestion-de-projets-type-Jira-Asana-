package com.projectsaas.notification.exception;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateName) {
        super("Template not found: " + templateName);
    }

    public TemplateNotFoundException(String templateName, Throwable cause) {
        super("Template not found: " + templateName, cause);
    }
}