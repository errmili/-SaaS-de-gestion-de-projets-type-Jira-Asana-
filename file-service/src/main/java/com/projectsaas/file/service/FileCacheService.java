package com.projectsaas.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.redis.host")
public class FileCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FILE_CACHE_PREFIX = "file:";
    private static final String TEMP_TOKEN_PREFIX = "temp_token:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public void cacheFileInfo(UUID fileId, Object fileInfo) {
        try {
            String key = FILE_CACHE_PREFIX + fileId;
            redisTemplate.opsForValue().set(key, fileInfo, DEFAULT_TTL);
            log.debug("Cached file info for: {}", fileId);
        } catch (Exception e) {
            log.error("Error caching file info", e);
        }
    }

    public Object getCachedFileInfo(UUID fileId) {
        try {
            String key = FILE_CACHE_PREFIX + fileId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting cached file info", e);
            return null;
        }
    }

    public void storeTempToken(String token, String filePath, Duration expiration) {
        try {
            String key = TEMP_TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(key, filePath, expiration);
            log.debug("Stored temp token: {}", token);
        } catch (Exception e) {
            log.error("Error storing temp token", e);
        }
    }

    public String getTempTokenPath(String token) {
        try {
            String key = TEMP_TOKEN_PREFIX + token;
            Object path = redisTemplate.opsForValue().get(key);
            return path != null ? path.toString() : null;
        } catch (Exception e) {
            log.error("Error getting temp token path", e);
            return null;
        }
    }

    public void invalidateFileCache(UUID fileId) {
        try {
            String key = FILE_CACHE_PREFIX + fileId;
            redisTemplate.delete(key);
            log.debug("Invalidated cache for file: {}", fileId);
        } catch (Exception e) {
            log.error("Error invalidating file cache", e);
        }
    }
}
