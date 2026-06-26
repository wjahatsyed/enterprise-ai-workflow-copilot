package com.wajahat.aiworkflow.user;

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
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    public UserResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return appUserService.create(tenantId, request);
    }

    @GetMapping
    public List<UserResponse> findByTenant(@PathVariable UUID tenantId) {
        return appUserService.findByTenant(tenantId);
    }
}
