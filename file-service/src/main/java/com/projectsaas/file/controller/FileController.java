package com.projectsaas.file.controller;

import com.projectsaas.file.dto.*;
import com.projectsaas.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "API pour la gestion des fichiers")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload d'un fichier")
    public ResponseEntity<ApiResponse<FileDto>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) UUID entityId,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "allowDuplicates", defaultValue = "true") Boolean allowDuplicates,
            @RequestHeader("Authorization") String authorization) {

        try {
            String token = authorization.replace("Bearer ", "");

            UploadFileRequest request = UploadFileRequest.builder()
                    .folderId(folderId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .isPublic(isPublic)
                    .allowDuplicates(allowDuplicates)
                    .build();

            FileDto uploadedFile = fileService.uploadFile(file, request, token);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(uploadedFile, "Fichier uploadé avec succès"));

        } catch (IllegalArgumentException e) {
            log.error("Invalid file upload request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur inattendue lors de l'upload"));
        }
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Obtenir les informations d'un fichier")
    public ResponseEntity<ApiResponse<FileDto>> getFile(
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        FileDto file = fileService.getFileById(fileId, token);

        return ResponseEntity.ok(ApiResponse.success(file));
    }

    @GetMapping("/{fileId}/download")
    @Operation(summary = "Télécharger un fichier")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authorization,
            HttpServletResponse response) throws IOException {

        String token = authorization.replace("Bearer ", "");
        FileDownloadResponse downloadResponse = fileService.downloadFile(fileId, token);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + downloadResponse.getFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, downloadResponse.getContentType());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(downloadResponse.getFileSize()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(downloadResponse.getInputStream()));
    }

    @GetMapping
    @Operation(summary = "Lister les fichiers avec pagination")
    public ResponseEntity<ApiResponse<Page<FileDto>>> getFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID folderId,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        FileSearchRequest searchRequest = FileSearchRequest.builder()
                .search(search)
                .folderId(folderId)
                .build();

        Page<FileDto> files = fileService.getFiles(searchRequest, pageable, token);

        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/folder/{folderId}")
    @Operation(summary = "Obtenir les fichiers d'un dossier")
    public ResponseEntity<ApiResponse<List<FileDto>>> getFolderFiles(
            @PathVariable(required = false) UUID folderId,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        List<FileDto> files = fileService.getFolderFiles(folderId, token);

        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Supprimer un fichier")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        fileService.deleteFile(fileId, token);

        return ResponseEntity.ok(ApiResponse.success(null, "Fichier supprimé avec succès"));
    }

    @PostMapping("/{fileId}/share")
    @Operation(summary = "Partager un fichier")
    public ResponseEntity<ApiResponse<FileShareDto>> shareFile(
            @PathVariable UUID fileId,
            @Valid @RequestBody ShareFileRequest request,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        FileShareDto share = fileService.shareFile(fileId, request, token);

        return ResponseEntity.ok(ApiResponse.success(share, "Fichier partagé avec succès"));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des fichiers")
    public ResponseEntity<ApiResponse<List<FileDto>>> searchFiles(
            @RequestParam String q,
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        List<FileDto> files = fileService.searchFiles(q, token);

        return ResponseEntity.ok(ApiResponse.success(files));
    }
}