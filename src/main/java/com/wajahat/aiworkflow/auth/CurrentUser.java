package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.user.UserRole;
import java.util.UUID;

public record CurrentUser(
        UUID userId,
        UUID tenantId,
        String email,
        UserRole role
) {
}
