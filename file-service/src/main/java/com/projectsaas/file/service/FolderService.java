package com.projectsaas.file.service;

import com.projectsaas.file.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FolderService {

    // Implémentation basique pour que ça compile
    public FolderDto createFolder(CreateFolderRequest request, String token) {
        // TODO: Implémenter
        return FolderDto.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .build();
    }

    public List<FolderDto> getFolderTree(String token) {
        // TODO: Implémenter
        return List.of();
    }

    public FolderDto getFolderById(UUID folderId, String token) {
        // TODO: Implémenter
        return FolderDto.builder()
                .id(folderId)
                .name("Test Folder")
                .build();
    }

    public void deleteFolder(UUID folderId, boolean force, String token) {
        // TODO: Implémenter
        log.info("Deleting folder: {}", folderId);
    }
}