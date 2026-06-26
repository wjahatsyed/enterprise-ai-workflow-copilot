package com.wajahat.aiworkflow.tenant;

import com.wajahat.aiworkflow.agent.Agent;
import com.wajahat.aiworkflow.agent.AgentRepository;
import com.wajahat.aiworkflow.agent.AgentStatus;
import com.wajahat.aiworkflow.auth.CurrentUser;
import com.wajahat.aiworkflow.auth.JwtTokenService;
import com.wajahat.aiworkflow.document.Document;
import com.wajahat.aiworkflow.document.DocumentRepository;
import com.wajahat.aiworkflow.document.DocumentSourceType;
import com.wajahat.aiworkflow.document.DocumentStatus;
import com.wajahat.aiworkflow.user.UserRole;
import com.wajahat.aiworkflow.workflow.Workflow;
import com.wajahat.aiworkflow.workflow.WorkflowRepository;
import com.wajahat.aiworkflow.workflow.WorkflowRun;
import com.wajahat.aiworkflow.workflow.WorkflowRunRepository;
import com.wajahat.aiworkflow.workflow.WorkflowRunStatus;
import com.wajahat.aiworkflow.workflow.WorkflowStatus;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TenantIsolationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowRunRepository workflowRunRepository;

    private Tenant currentTenant;
    private Tenant otherTenant;
    private String currentTenantToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        currentTenant = tenant("Tenant " + UUID.randomUUID(), "tenant-" + UUID.randomUUID());
        otherTenant = tenant("Tenant " + UUID.randomUUID(), "tenant-" + UUID.randomUUID());
        currentTenantToken = jwtTokenService.generateToken(new CurrentUser(
                UUID.randomUUID(),
                currentTenant.getId(),
                "admin@example.com",
                UserRole.TENANT_ADMIN
        ));
    }

    @Test
    void tenantIdPathMismatchShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/tenants/{tenantId}/users", otherTenant.getId())
                        .header("Authorization", bearer(currentTenantToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void workspaceFromAnotherTenantShouldReturnForbidden() throws Exception {
        Workspace workspace = workspace(otherTenant);

        mockMvc.perform(post("/api/workspaces/{workspaceId}/documents", workspace.getId())
                        .header("Authorization", bearer(currentTenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Policy\",\"content\":\"Tenant private content\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentFromAnotherTenantShouldReturnForbidden() throws Exception {
        Document document = document(workspace(otherTenant));

        mockMvc.perform(get("/api/documents/{documentId}", document.getId())
                        .header("Authorization", bearer(currentTenantToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void agentFromAnotherTenantShouldReturnForbidden() throws Exception {
        Agent agent = agent(workspace(otherTenant));

        mockMvc.perform(get("/api/agents/{agentId}", agent.getId())
                        .header("Authorization", bearer(currentTenantToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflowFromAnotherTenantShouldReturnForbidden() throws Exception {
        Workflow workflow = workflow(workspace(otherTenant));

        mockMvc.perform(get("/api/workflows/{workflowId}", workflow.getId())
                        .header("Authorization", bearer(currentTenantToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflowRunFromAnotherTenantShouldReturnForbidden() throws Exception {
        Workflow workflow = workflow(workspace(otherTenant));
        WorkflowRun run = new WorkflowRun();
        run.setWorkflow(workflow);
        run.setStatus(WorkflowRunStatus.COMPLETED);
        run.setInputJson("{\"input\":true}");
        run = workflowRunRepository.save(run);

        mockMvc.perform(get("/api/workflow-runs/{runId}", run.getId())
                        .header("Authorization", bearer(currentTenantToken)))
                .andExpect(status().isForbidden());
    }

    private Tenant tenant(String name, String slug) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setStatus("ACTIVE");
        return tenantRepository.save(tenant);
    }

    private Workspace workspace(Tenant tenant) {
        Workspace workspace = new Workspace();
        workspace.setTenant(tenant);
        workspace.setName("Workspace " + UUID.randomUUID());
        return workspaceRepository.save(workspace);
    }

    private Document document(Workspace workspace) {
        Document document = new Document();
        document.setWorkspace(workspace);
        document.setTitle("Document " + UUID.randomUUID());
        document.setSourceType(DocumentSourceType.MANUAL_TEXT);
        document.setStatus(DocumentStatus.READY);
        return documentRepository.save(document);
    }

    private Agent agent(Workspace workspace) {
        Agent agent = new Agent();
        agent.setWorkspace(workspace);
        agent.setName("Agent " + UUID.randomUUID());
        agent.setSystemPrompt("Be helpful.");
        agent.setModel("gpt-4o-mini");
        agent.setStatus(AgentStatus.ACTIVE);
        return agentRepository.save(agent);
    }

    private Workflow workflow(Workspace workspace) {
        Workflow workflow = new Workflow();
        workflow.setWorkspace(workspace);
        workflow.setName("Workflow " + UUID.randomUUID());
        workflow.setStatus(WorkflowStatus.ACTIVE);
        return workflowRepository.save(workflow);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
