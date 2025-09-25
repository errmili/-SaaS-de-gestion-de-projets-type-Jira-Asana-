package com.projectsaas.file.controller;
import com.projectsaas.file.dto.*;
import com.projectsaas.file.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Folder Management", description = "API pour la gestion des dossiers")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @Operation(summary = "Créer un nouveau dossier")
    public ResponseEntity<ApiResponse<FolderDto>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        FolderDto folder = folderService.createFolder(request, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(folder, "Dossier créé avec succès"));
    }

    @GetMapping
    @Operation(summary = "Obtenir l'arborescence des dossiers")
    public ResponseEntity<ApiResponse<List<FolderDto>>> getFolderTree(
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        List<FolderDto> tree = folderService.getFolderTree(token);

        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/{folderId}")
    @Operation(summary = "Obtenir un dossier par ID")
    public ResponseEntity<ApiResponse<FolderDto>> getFolder(
            @PathVariable UUID folderId,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        FolderDto folder = folderService.getFolderById(folderId, token);

        return ResponseEntity.ok(ApiResponse.success(folder));
    }

    @DeleteMapping("/{folderId}")
    @Operation(summary = "Supprimer un dossier")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable UUID folderId,
            @RequestParam(defaultValue = "false") boolean force,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        folderService.deleteFolder(folderId, force, token);

        return ResponseEntity.ok(ApiResponse.success(null, "Dossier supprimé avec succès"));
    }
}