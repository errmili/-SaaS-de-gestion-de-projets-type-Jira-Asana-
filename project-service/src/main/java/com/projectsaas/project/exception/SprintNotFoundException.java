// SprintNotFoundException.java
package com.projectsaas.project.exception;

public class SprintNotFoundException extends RuntimeException {
    public SprintNotFoundException(String message) {
        super(message);
    }

    public SprintNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}