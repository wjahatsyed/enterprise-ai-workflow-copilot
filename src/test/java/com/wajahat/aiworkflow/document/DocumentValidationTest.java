package com.wajahat.aiworkflow.document;

import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.tenant.TenantRepository;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@WithMockUser(roles = "TENANT_ADMIN")
public class DocumentValidationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void whenWorkspaceIdIsLiteralString_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/documents", "workspaceId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"content\":\"Content\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'workspaceId' has invalid value 'workspaceId'. Expected type is 'UUID'"))
                .andExpect(jsonPath("$.details.parameter").value("workspaceId"));
    }

    @Test
    void whenWorkspaceIdDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(post("/api/workspaces/{workspaceId}/documents", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"content\":\"Content\"}"))
                .andExpect(status().isNotFound());
    }
}
