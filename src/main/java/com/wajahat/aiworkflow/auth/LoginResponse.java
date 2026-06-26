package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.user.UserRole;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        UUID tenantId,
        String email,
        UserRole role
) {
}
