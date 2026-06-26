package com.wajahat.aiworkflow.tenant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tenants", description = "Tenant administration")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @Operation(summary = "Create tenant", description = "Creates a tenant. Requires TENANT_ADMIN.")
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.create(request);
    }

    @GetMapping
    @Operation(summary = "List tenants", description = "Lists tenants visible to tenant administrators.")
    public List<TenantResponse> findAll() {
        return tenantService.findAll();
    }
}
