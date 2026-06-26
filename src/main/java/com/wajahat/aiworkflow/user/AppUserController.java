package com.wajahat.aiworkflow.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Tenant user administration")
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a user in a tenant. Requires TENANT_ADMIN.")
    public UserResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return appUserService.create(tenantId, request);
    }

    @GetMapping
    @Operation(summary = "List tenant users", description = "Lists users for the tenant. Requires TENANT_ADMIN.")
    public List<UserResponse> findByTenant(@PathVariable UUID tenantId) {
        return appUserService.findByTenant(tenantId);
    }
}
