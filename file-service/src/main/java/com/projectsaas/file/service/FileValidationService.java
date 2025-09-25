package com.projectsaas.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

@Service
@Slf4j
public class FileValidationService {

    public ValidationResult validateFile(MultipartFile file) {
        ValidationResult result = new ValidationResult();

        if (file == null || file.isEmpty()) {
            result.addError("Le fichier est vide");
            return result;
        }

        // Validation basique - étendre selon vos besoins
        if (file.getSize() > 100 * 1024 * 1024) { // 100MB
            result.addError("Fichier trop volumineux (max 100MB)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            result.addError("Nom de fichier requis");
        }

        return result;
    }

    public String calculateChecksum(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            log.error("Error calculating checksum", e);
            return null;
        }
    }
}