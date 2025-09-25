package com.projectsaas.project.controller;

import com.projectsaas.project.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "project-service",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Service is healthy", health)
        );
    }
}
