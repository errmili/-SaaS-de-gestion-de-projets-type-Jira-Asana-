// ===========================================
// TenantAlreadyExistsException.java - Tenant existe déjà
// ===========================================
package com.projectsaas.auth.exception;

public class TenantAlreadyExistsException extends RuntimeException {

    public TenantAlreadyExistsException(String message) {
        super(message);
    }

    public TenantAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}