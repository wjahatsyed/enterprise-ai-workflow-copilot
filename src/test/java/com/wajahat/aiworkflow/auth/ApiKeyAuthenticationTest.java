package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.tenant.TenantRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ApiKeyAuthenticationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldAuthenticateWithValidApiKey() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        tenant.setSlug("test-tenant");
        tenant.setStatus("ACTIVE");
        tenant = tenantRepository.save(tenant);

        String rawKey = "test-api-key-123";
        String hash = hashApiKey(rawKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setTenant(tenant);
        apiKey.setName("Test Key");
        apiKey.setApiKeyHash(hash);
        apiKey.setEnabled(true);
        apiKeyRepository.save(apiKey);

        mockMvc.perform(get("/api/tenants")
                        .header("X-API-Key", rawKey))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/tenants")
                        .header("X-API-Key", "invalid-key"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldFailWithDisabledApiKey() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant 2");
        tenant.setSlug("test-tenant-2");
        tenant.setStatus("ACTIVE");
        tenant = tenantRepository.save(tenant);

        String rawKey = "disabled-key";
        String hash = hashApiKey(rawKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setTenant(tenant);
        apiKey.setName("Disabled Key");
        apiKey.setApiKeyHash(hash);
        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);

        mockMvc.perform(get("/api/tenants")
                        .header("X-API-Key", rawKey))
                .andExpect(status().is4xxClientError());
    }

    private String hashApiKey(String apiKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
