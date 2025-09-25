package com.projectsaas.auth.repository;

import com.projectsaas.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Recherche par email et tenant (pour login)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenant.id = :tenantId")
    Optional<User> findByEmailAndTenantId(@Param("email") String email,
                                          @Param("tenantId") UUID tenantId);

    // Recherche par email et subdomain (plus pratique)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenant.subdomain = :subdomain")
    Optional<User> findByEmailAndTenantSubdomain(@Param("email") String email,
                                                 @Param("subdomain") String subdomain);

    // Tous les utilisateurs d'un tenant
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.isActive = true")
    List<User> findByTenantIdAndIsActiveTrue(@Param("tenantId") UUID tenantId);

    // Vérifier si email existe dans un tenant
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.tenant.id = :tenantId")
    boolean existsByEmailAndTenantId(@Param("email") String email,
                                     @Param("tenantId") UUID tenantId);

    // Compter utilisateurs actifs par tenant (pour les limites de plan)
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.isActive = true")
    long countActiveUsersByTenantId(@Param("tenantId") UUID tenantId);

    // Recherche par ID avec tenant (sécurité)
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenant.id = :tenantId")
    Optional<User> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}