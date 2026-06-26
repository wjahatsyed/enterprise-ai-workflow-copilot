package com.wajahat.aiworkflow.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wajahat.aiworkflow.user.UserRole;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    @Value("${security.jwt.secret:portfolio-demo-secret-change-me-please}")
    private String secret;

    @Value("${security.jwt.expiration-seconds:86400}")
    private long expirationSeconds;

    public String generateToken(CurrentUser user) {
        try {
            Instant now = Instant.now();

            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", user.userId().toString());
            claims.put("tenantId", user.tenantId().toString());
            claims.put("email", user.email());
            claims.put("role", user.role().name());
            claims.put("iat", now.getEpochSecond());
            claims.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

            String headerPart = encodeJson(header);
            String claimsPart = encodeJson(claims);
            String unsignedToken = headerPart + "." + claimsPart;

            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    public Optional<CurrentUser> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> claims = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    CLAIMS_TYPE
            );

            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiresAt) {
                return Optional.empty();
            }

            return Optional.of(new CurrentUser(
                    UUID.fromString((String) claims.get("sub")),
                    UUID.fromString((String) claims.get("tenantId")),
                    (String) claims.get("email"),
                    UserRole.valueOf((String) claims.get("role"))
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String first, String second) {
        byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);
        byte[] secondBytes = second.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(firstBytes, secondBytes);
    }
}
