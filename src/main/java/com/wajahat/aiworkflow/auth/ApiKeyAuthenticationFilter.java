package com.wajahat.aiworkflow.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // If already authenticated by JWT, skip API key authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String apiKey = request.getHeader(API_KEY_HEADER);
            if (apiKey != null && !apiKey.isBlank()) {
                apiKeyService.validateApiKey(apiKey).ifPresent(this::authenticate);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(CurrentUser currentUser) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        currentUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + currentUser.role().name()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
