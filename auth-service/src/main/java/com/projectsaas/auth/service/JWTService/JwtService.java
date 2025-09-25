package com.projectsaas.auth.service.JWTService;

import com.projectsaas.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // Méthodes pour User (entité)
    public String generateToken(User user, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("isRefreshToken", false);

        return buildToken(extraClaims, user.getEmail(), jwtExpiration);
    }

    public String generateRefreshToken(User user, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("isRefreshToken", true);

        return buildToken(extraClaims, user.getEmail(), refreshExpiration);
    }

    // Méthodes pour UserDetails (Spring Security)
    public String generateToken(UserDetails userDetails, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        extraClaims.put("authorities", userDetails.getAuthorities());
        extraClaims.put("isRefreshToken", false);

        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        extraClaims.put("isRefreshToken", true);

        return buildToken(extraClaims, userDetails.getUsername(), refreshExpiration);
    }

    // Construire le token
    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Valider token pour User
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            final UUID tokenTenantId = extractTenantId(token);

            boolean isValidUser = username.equals(user.getEmail());
            boolean isValidTenant = tokenTenantId != null && tokenTenantId.equals(user.getTenant().getId());
            boolean isNotExpired = !isTokenExpired(token);

            return isValidUser && isValidTenant && isNotExpired;
        } catch (Exception e) {
            log.error("Error validating token for User: {}", e.getMessage());
            return false;
        }
    }

    // Valider token pour UserDetails
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validating token for UserDetails: {}", e.getMessage());
            return false;
        }
    }

    // Vérifier si token est valide (sans utilisateur)
    public boolean isValidToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Extraire username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraire tenant ID
    public UUID extractTenantId(String token) {
        String tenantIdStr = extractClaim(token, claims -> claims.get("tenantId", String.class));
        return tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;
    }

    // Extraire user ID
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    // Extraire firstName
    public String extractFirstName(String token) {
        return extractClaim(token, claims -> claims.get("firstName", String.class));
    }

    // Extraire lastName
    public String extractLastName(String token) {
        return extractClaim(token, claims -> claims.get("lastName", String.class));
    }

    // Extraire expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraire claim générique
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraire tous les claims
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT signature does not match: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    // Vérifier si token expiré
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Obtenir clé de signature
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Vérifier si c'est un refresh token
    public boolean isRefreshToken(String token) {
        try {
            Boolean isRefresh = extractClaim(token, claims -> claims.get("isRefreshToken", Boolean.class));
            return isRefresh != null && isRefresh;
        } catch (Exception e) {
            return false;
        }
    }

    // Obtenir temps restant avant expiration
    public long getExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}