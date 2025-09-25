// ===========================================
// TenantNotFoundException.java - Tenant non trouvé
// ===========================================
package com.projectsaas.auth.exception;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
