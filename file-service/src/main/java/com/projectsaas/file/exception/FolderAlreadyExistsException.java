package com.projectsaas.file.exception;

public class FolderAlreadyExistsException extends FileException {
    public FolderAlreadyExistsException(String message) {
        super(message);
    }
}