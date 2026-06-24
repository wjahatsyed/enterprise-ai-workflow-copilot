package com.wajahat.aiworkflow.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID tenantId,
        String fullName,
        String email,
        UserRole role
) {}