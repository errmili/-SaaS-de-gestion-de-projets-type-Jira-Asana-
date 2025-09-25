package com.projectsaas.file.repository;

import com.projectsaas.file.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    // Méthodes principales avec tenant
    List<Folder> findByTenantIdOrderByPathAsc(UUID tenantId);

    Optional<Folder> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Folder> findByParentIdAndTenantId(UUID parentId, UUID tenantId);

    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND f.parentId IS NULL ORDER BY f.name ASC")
    List<Folder> findRootFoldersByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(f) FROM Folder f WHERE f.parentId = :parentId AND f.tenantId = :tenantId")
    Integer countSubFoldersByParentIdAndTenantId(@Param("parentId") UUID parentId, @Param("tenantId") UUID tenantId);

    // Version simplifiée pour compatibilité
    @Query("SELECT COUNT(f) FROM Folder f WHERE f.parentId = :parentId")
    Integer countSubFoldersByParentId(@Param("parentId") UUID parentId);

    boolean existsByNameAndParentIdAndTenantId(String name, UUID parentId, UUID tenantId);

    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Folder> searchByNameAndTenantId(@Param("search") String search, @Param("tenantId") UUID tenantId);

    // Méthodes pour compter les fichiers dans un dossier
    @Query("SELECT COUNT(file) FROM FileEntity file WHERE file.folderId = :folderId")
    Integer countFilesByFolderId(@Param("folderId") UUID folderId);

    // Méthodes pour la hiérarchie de dossiers
    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND f.path LIKE CONCAT(:parentPath, '%') ORDER BY f.path ASC")
    List<Folder> findByTenantIdAndPathStartingWith(@Param("tenantId") UUID tenantId, @Param("parentPath") String parentPath);

    // Trouver tous les descendants d'un dossier
    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND f.parentId = :parentId ORDER BY f.name ASC")
    List<Folder> findChildrenByParentIdAndTenantId(@Param("parentId") UUID parentId, @Param("tenantId") UUID tenantId);

    // Vérifier la profondeur maximale
    @Query("SELECT MAX(f.depth) FROM Folder f WHERE f.tenantId = :tenantId")
    Integer getMaxDepthByTenantId(@Param("tenantId") UUID tenantId);

    // Trouver les dossiers par niveau de profondeur
    List<Folder> findByTenantIdAndDepthOrderByNameAsc(UUID tenantId, Integer depth);

    // Compter le nombre total de dossiers pour un tenant
    Long countByTenantId(UUID tenantId);

    // Méthodes pour les statistiques
    @Query("SELECT f.depth, COUNT(f) FROM Folder f WHERE f.tenantId = :tenantId GROUP BY f.depth ORDER BY f.depth")
    List<Object[]> getFolderStatsByDepth(@Param("tenantId") UUID tenantId);
}