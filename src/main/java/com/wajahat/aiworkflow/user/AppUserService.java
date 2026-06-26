package com.wajahat.aiworkflow.user;

import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.tenant.TenantAccessValidator;
import com.wajahat.aiworkflow.tenant.TenantRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final TenantAccessValidator tenantAccessValidator;

    public UserResponse create(UUID tenantId, CreateUserRequest request) {
        tenantAccessValidator.validateTenantId(tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (appUserRepository.existsByTenantIdAndEmail(tenantId, request.email())) {
            throw new IllegalArgumentException("User email already exists for tenant");
        }

        AppUser user = new AppUser();
        user.setTenant(tenant);
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(request.role());

        return toResponse(appUserRepository.save(user));
    }

    public List<UserResponse> findByTenant(UUID tenantId) {
        tenantAccessValidator.validateTenantId(tenantId);
        return appUserRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
