package com.projectsaas.file.exception.handler;
import com.projectsaas.file.dto.ApiResponse;
import com.projectsaas.file.exception.DuplicateFileException;
import com.projectsaas.file.exception.FileDownloadException;
import com.projectsaas.file.exception.FileNotFoundException;
import com.projectsaas.file.exception.FilePermissionException;
import com.projectsaas.file.exception.FileSecurityException;
import com.projectsaas.file.exception.FileUploadException;
import com.projectsaas.file.exception.FileValidationException;
import com.projectsaas.file.exception.FolderAlreadyExistsException;
import com.projectsaas.file.exception.FolderNotFoundException;
import com.projectsaas.file.exception.UnauthorizedException;
import com.projectsaas.file.storage.StorageException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileNotFound(FileNotFoundException ex) {
        log.error("File not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("File not found", ex.getMessage()));
    }

    @ExceptionHandler(FolderNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFolderNotFound(FolderNotFoundException ex) {
        log.error("Folder not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Folder not found", ex.getMessage()));
    }

    @ExceptionHandler(FilePermissionException.class)
    public ResponseEntity<ApiResponse<Object>> handleFilePermission(FilePermissionException ex) {
        log.error("File permission denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Permission denied", ex.getMessage()));
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileValidation(FileValidationException ex) {
        log.error("File validation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("File validation failed", ex.getMessage()));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUpload(FileUploadException ex) {
        log.error("File upload failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("File upload failed", ex.getMessage()));
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileDownload(FileDownloadException ex) {
        log.error("File download failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("File download failed", ex.getMessage()));
    }

    @ExceptionHandler(FileSecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileSecurity(FileSecurityException ex) {
        log.error("File security issue: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Security issue", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateFile(DuplicateFileException ex) {
        log.error("Duplicate file: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Duplicate file", ex.getMessage()));
    }

    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleFolderAlreadyExists(FolderAlreadyExistsException ex) {
        log.error("Folder already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Folder already exists", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.error("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("File too large", "Maximum upload size exceeded"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors.toString()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "Please try again later"));
    }

    // 2. ✅ Handler pour UnauthorizedException (MANQUANT)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", ex.getMessage()));
    }

    // 3. ✅ Handler pour StorageException (MANQUANT si vous utilisez le package storage)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Object>> handleStorageException(StorageException ex) {
        log.error("Storage error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Storage error", ex.getMessage()));
    }

    // 4. ✅ Handler pour IllegalArgumentException (RECOMMANDÉ)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid argument", ex.getMessage()));
    }

    // 5. ✅ Handler pour AccessDeniedException (RECOMMANDÉ pour Spring Security)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "You don't have permission to access this resource"));
    }

    // 6. ✅ Handler pour AuthenticationException (RECOMMANDÉ pour Spring Security)
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(org.springframework.security.core.AuthenticationException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", "Invalid credentials"));
    }

    // 7. ✅ Handler pour DataIntegrityViolationException (RECOMMANDÉ pour contraintes DB)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Data conflict", "A data constraint was violated"));
    }

    // 8. ✅ Handler pour HttpRequestMethodNotSupportedException
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        log.error("Method not supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Method not allowed", ex.getMessage()));
    }

    // 9. ✅ Handler pour HttpMediaTypeNotSupportedException
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupported(org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        log.error("Media type not supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("Unsupported media type", ex.getMessage()));
    }

    // 10. ✅ Handler pour MissingServletRequestParameterException
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameter(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        log.error("Missing parameter: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Missing parameter", "Required parameter '" + ex.getParameterName() + "' is missing"));
    }

    // 11. ✅ Handler pour ConstraintViolationException (Validation)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors.toString()));
    }

    // 12. ✅ Handler pour IOException (Erreurs I/O)
    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(java.io.IOException ex) {
        log.error("I/O error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("I/O error", "An error occurred while processing the file"));
    }
}