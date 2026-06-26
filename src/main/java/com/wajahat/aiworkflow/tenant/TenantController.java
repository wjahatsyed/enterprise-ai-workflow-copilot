package com.wajahat.aiworkflow.tenant;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.create(request);
    }

    @GetMapping
    public List<TenantResponse> findAll() {
        return tenantService.findAll();
    }
}
