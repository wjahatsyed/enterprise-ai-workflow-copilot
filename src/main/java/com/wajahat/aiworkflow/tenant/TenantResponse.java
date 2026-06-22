package com.wajahat.aiworkflow.tenant;

import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String status
) {}