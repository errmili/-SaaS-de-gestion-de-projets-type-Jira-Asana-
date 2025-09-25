// ===========================================
// TenantDto.java - DTO tenant/organisation
// ===========================================
package com.projectsaas.auth.dto;

import com.projectsaas.auth.entity.Tenant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDto {

    private UUID id;
    private String name;
    private String subdomain;
    private Tenant.PlanType planType;
    private Integer maxUsers;
    private Integer currentUsers;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

