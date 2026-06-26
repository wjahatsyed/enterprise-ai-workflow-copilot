package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.user.AppUser;
import com.wajahat.aiworkflow.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CurrentUser currentUser = new CurrentUser(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(
                jwtTokenService.generateToken(currentUser),
                "Bearer",
                currentUser.userId(),
                currentUser.tenantId(),
                currentUser.email(),
                currentUser.role()
        );
    }
}
