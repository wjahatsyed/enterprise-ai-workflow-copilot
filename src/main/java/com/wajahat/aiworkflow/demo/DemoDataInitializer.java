package com.wajahat.aiworkflow.demo;

import com.wajahat.aiworkflow.agent.*;
import com.wajahat.aiworkflow.document.*;
import com.wajahat.aiworkflow.tenant.*;
import com.wajahat.aiworkflow.user.*;
import com.wajahat.aiworkflow.workspace.*;
import com.wajahat.aiworkflow.workflow.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final AppUserRepository userRepository;
    private final AppUserService userService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceService workspaceService;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final AgentRepository agentRepository;
    private final AgentService agentService;
    private final WorkflowRepository workflowRepository;
    private final WorkflowService workflowService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing demo data...");
        try {
            // 1. Create Tenant
            Tenant tenant = ensureTenant("Acme Logistics", "acme-logistics");
            UUID tenantId = tenant.getId();

            // Set TenantContext for downstream services that use TenantAccessValidator
            TenantContext.setTenantId(tenantId);

            // 2. Create Tenant Admin User
            ensureUser(tenantId, "Demo Admin", "demo@acme.com", UserRole.TENANT_ADMIN);

            // 3. Create Workspace
            Workspace workspace = ensureWorkspace(tenantId, "Customer Operations", "Operations workspace for customer support and logistics");
            UUID workspaceId = workspace.getId();

            // 4. Create Documents
            ensureDocument(workspaceId, "Refund Policy",
                    "Our refund policy allows customers to request a refund within 30 days of purchase. " +
                    "Refunds are processed to the original payment method. " +
                    "Items must be in original condition. " +
                    "For logistics delays over 48 hours, a partial shipping refund is automatically granted.");

            ensureDocument(workspaceId, "Escalation SOP",
                    "Standard Operating Procedure for Escalations: " +
                    "1. Identify priority (Low, Medium, High, Critical). " +
                    "2. High and Critical cases must be escalated to a Manager. " +
                    "3. For technical issues, route to the Engineering team. " +
                    "4. For customer complaints regarding late delivery, route to Operations lead.");

            // 5. Create Agent
            ensureAgent(workspaceId, "Customer Support Agent",
                    "AI agent for handling customer support queries and logistics coordination.",
                    "You are a helpful and efficient Customer Support Agent for Acme Logistics. " +
                    "Use the provided context to answer customer questions accurately. " +
                    "If you don't know the answer, ask for more details or escalate to a human.");

            // 6. Create Workflow
            ensureWorkflow(workspaceId, "Complaint Review Workflow",
                    "A workflow to review and process customer complaints.",
                    List.of(
                            new WorkflowStepRequest("Initial AI Analysis", WorkflowStepType.AI_AGENT,
                                    "{\"agentName\":\"Customer Support Agent\",\"promptTemplate\":\"Analyze this complaint: {{input}}\"}"),
                            new WorkflowStepRequest("Manager Approval", WorkflowStepType.HUMAN_APPROVAL,
                                    "{\"approver\":\"demo@acme.com\"}"),
                            new WorkflowStepRequest("Final Response", WorkflowStepType.EXTERNAL_ACTION,
                                    "{\"actionName\":\"SendEmail\",\"parameters\":{\"template\":\"complaint_resolved\"}}")
                    ));

            log.info("Demo data initialization completed successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize demo data", e);
        } finally {
            TenantContext.clear();
        }
    }

    private Tenant ensureTenant(String name, String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseGet(() -> {
                    log.info("Creating tenant: {}", name);
                    tenantService.create(new CreateTenantRequest(name, slug));
                    return tenantRepository.findBySlug(slug).orElseThrow();
                });
    }

    private void ensureUser(UUID tenantId, String fullName, String email, UserRole role) {
        if (!userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            log.info("Creating user: {}", email);
            userService.create(tenantId, new CreateUserRequest(fullName, email, role));
        }
    }

    private Workspace ensureWorkspace(UUID tenantId, String name, String description) {
        return workspaceRepository.findByTenantId(tenantId).stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating workspace: {}", name);
                    workspaceService.create(tenantId, new CreateWorkspaceRequest(name, description));
                    return workspaceRepository.findByTenantId(tenantId).stream()
                            .filter(w -> w.getName().equals(name))
                            .findFirst()
                            .orElseThrow();
                });
    }

    private void ensureDocument(UUID workspaceId, String title, String content) {
        boolean exists = documentRepository.findByWorkspaceId(workspaceId).stream()
                .anyMatch(d -> d.getTitle().equals(title));
        if (!exists) {
            log.info("Creating document: {}", title);
            documentService.create(workspaceId, new CreateDocumentRequest(title, content));
        }
    }

    private void ensureAgent(UUID workspaceId, String name, String description, String systemPrompt) {
        boolean exists = agentRepository.findByWorkspaceId(workspaceId).stream()
                .anyMatch(a -> a.getName().equals(name));
        if (!exists) {
            log.info("Creating agent: {}", name);
            agentService.create(workspaceId, new CreateAgentRequest(name, description, systemPrompt, null));
        }
    }

    private void ensureWorkflow(UUID workspaceId, String name, String description, List<WorkflowStepRequest> steps) {
        boolean exists = workflowRepository.findByWorkspaceId(workspaceId).stream()
                .anyMatch(w -> w.getName().equals(name));
        if (!exists) {
            log.info("Creating workflow: {}", name);
            workflowService.create(workspaceId, new CreateWorkflowRequest(name, description, steps));
        }
    }
}
