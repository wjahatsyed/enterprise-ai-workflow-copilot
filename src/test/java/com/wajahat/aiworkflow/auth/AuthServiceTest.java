package com.wajahat.aiworkflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.user.AppUser;
import com.wajahat.aiworkflow.user.AppUserRepository;
import com.wajahat.aiworkflow.user.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void loginShouldReturnBearerTokenAndUserClaims() {
        AppUserRepository appUserRepository = org.mockito.Mockito.mock(AppUserRepository.class);
        JwtTokenService jwtTokenService = org.mockito.Mockito.mock(JwtTokenService.class);
        AuthService authService = new AuthService(appUserRepository, jwtTokenService);
        Tenant tenant = new Tenant();
        AppUser user = new AppUser();
        user.setTenant(tenant);
        user.setEmail("wajahat@example.com");
        user.setRole(UserRole.TENANT_ADMIN);

        when(appUserRepository.findByEmail("wajahat@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(new CurrentUser(
                user.getId(),
                tenant.getId(),
                user.getEmail(),
                user.getRole()
        ))).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("wajahat@example.com"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.tenantId()).isEqualTo(tenant.getId());
        assertThat(response.email()).isEqualTo("wajahat@example.com");
        assertThat(response.role()).isEqualTo(UserRole.TENANT_ADMIN);
    }
}
