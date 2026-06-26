package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.user.UserRole;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    @Transactional(readOnly = true)
    public Optional<CurrentUser> validateApiKey(String apiKey) {
        String hash = hashApiKey(apiKey);
        return apiKeyRepository.findByApiKeyHashAndEnabledTrue(hash)
                .map(key -> new CurrentUser(
                        null, // API keys are for tenants, not specific users
                        key.getTenant().getId(),
                        "api-key-" + key.getName(),
                        UserRole.TENANT_ADMIN // API keys act as tenant admin
                ));
    }

    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
