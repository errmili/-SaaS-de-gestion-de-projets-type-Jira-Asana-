package com.projectsaas.file.exception;

public class DuplicateFileException extends RuntimeException {
    public DuplicateFileException(String message) {
        super(message);
    }

    public DuplicateFileException(String message, Throwable cause) {
        super(message, cause);
    }
}