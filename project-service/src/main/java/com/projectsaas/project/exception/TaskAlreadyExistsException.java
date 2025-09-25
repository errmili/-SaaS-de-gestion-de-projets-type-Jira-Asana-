package com.projectsaas.project.exception;

public class TaskAlreadyExistsException extends RuntimeException {
    public TaskAlreadyExistsException(String message) {
        super(message);
    }

    public TaskAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}