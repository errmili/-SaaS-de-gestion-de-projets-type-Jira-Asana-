package com.projectsaas.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuditEventService {

    @Async
    public void logFileUpload(UUID fileId, String filename, UUID userId, UUID tenantId) {
        log.info("AUDIT: File uploaded - fileId={}, filename={}, userId={}, tenantId={}",
                fileId, filename, userId, tenantId);
    }

    @Async
    public void logFileDownload(UUID fileId, UUID userId, UUID tenantId) {
        log.info("AUDIT: File downloaded - fileId={}, userId={}, tenantId={}",
                fileId, userId, tenantId);
    }

    @Async
    public void logFileDelete(UUID fileId, UUID userId, UUID tenantId) {
        log.info("AUDIT: File deleted - fileId={}, userId={}, tenantId={}",
                fileId, userId, tenantId);
    }

    @Async
    public void logFileShare(UUID fileId, UUID sharedBy, UUID sharedWith, UUID tenantId) {
        log.info("AUDIT: File shared - fileId={}, sharedBy={}, sharedWith={}, tenantId={}",
                fileId, sharedBy, sharedWith, tenantId);
    }
}