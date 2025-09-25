package com.projectsaas.file.storage;


/**
 * Exception pour les erreurs de stockage
 */
public class StorageException extends RuntimeException {
    private final String provider;
    private final String operation;

    public StorageException(String provider, String operation, String message) {
        super(String.format("[%s:%s] %s", provider, operation, message));
        this.provider = provider;
        this.operation = operation;
    }

    public StorageException(String provider, String operation, String message, Throwable cause) {
        super(String.format("[%s:%s] %s", provider, operation, message), cause);
        this.provider = provider;
        this.operation = operation;
    }

    public StorageException(String message) {
        super(message);
        this.provider = "unknown";
        this.operation = "unknown";
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.provider = "unknown";
        this.operation = "unknown";
    }

    public String getProvider() {
        return provider;
    }

    public String getOperation() {
        return operation;
    }
}