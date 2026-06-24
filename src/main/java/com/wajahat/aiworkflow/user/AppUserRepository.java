package com.wajahat.aiworkflow.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    List<AppUser> findByTenantId(UUID tenantId);
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}