package com.wajahat.aiworkflow.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByApiKeyHashAndEnabledTrue(String apiKeyHash);
}
