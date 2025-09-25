package com.projectsaas.auth.controller;

import com.projectsaas.auth.dto.ApiResponse;
import com.projectsaas.auth.dto.CreateTenantRequest;
import com.projectsaas.auth.dto.TenantDto;
import com.projectsaas.auth.service.BusinessServices.TenantService;
import com.projectsaas.auth.service.JWTService.TenantResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TenantDto>> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        log.info("Creating new tenant: {} with subdomain: {}", request.getName(), request.getSubdomain());

        TenantDto tenant = tenantService.createTenant(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization created successfully", tenant));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<TenantDto>> getCurrentTenant() {
        log.info("Getting current tenant info");

        TenantDto tenant = tenantService.getCurrentTenant();

        return ResponseEntity.ok(
                ApiResponse.success("Current tenant information", tenant)
        );
    }

    @GetMapping("/check-subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<Boolean>> checkSubdomainAvailability(@PathVariable String subdomain) {
        log.info("Checking subdomain availability: {}", subdomain);

        boolean available = tenantService.isSubdomainAvailable(subdomain);

        return ResponseEntity.ok(
                ApiResponse.success("Subdomain availability check", available)
        );
    }
}
