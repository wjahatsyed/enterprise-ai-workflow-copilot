package com.wajahat.aiworkflow.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.wajahat.aiworkflow.config.JacksonConfig;
import com.wajahat.aiworkflow.user.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {JwtTokenService.class, JacksonConfig.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-for-jwt-token-service",
        "security.jwt.expiration-seconds=3600"
})
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void generatedTokenShouldValidateToCurrentUser() {
        CurrentUser user = new CurrentUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "wajahat@example.com",
                UserRole.TENANT_ADMIN
        );

        String token = jwtTokenService.generateToken(user);
        Optional<CurrentUser> validatedUser = jwtTokenService.validateToken(token);

        assertThat(validatedUser).isPresent();
        assertThat(validatedUser.get()).isEqualTo(user);
    }

    @Test
    void tamperedTokenShouldNotValidate() {
        CurrentUser user = new CurrentUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "member@example.com",
                UserRole.MEMBER
        );

        String token = jwtTokenService.generateToken(user);

        assertThat(jwtTokenService.validateToken(token + "tampered")).isEmpty();
    }
}
