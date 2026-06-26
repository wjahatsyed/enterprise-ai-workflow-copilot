package com.wajahat.aiworkflow.tenant;

import com.wajahat.aiworkflow.agent.Agent;
import com.wajahat.aiworkflow.document.Document;
import com.wajahat.aiworkflow.exception.TenantMismatchException;
import com.wajahat.aiworkflow.user.AppUser;
import com.wajahat.aiworkflow.workflow.Workflow;
import com.wajahat.aiworkflow.workflow.WorkflowRun;
import com.wajahat.aiworkflow.workspace.Workspace;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantAccessValidator {

    public void validateTenantId(UUID tenantId) {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId != null && !currentTenantId.equals(tenantId)) {
            throw new TenantMismatchException("Access denied: tenant mismatch");
        }
    }

    public void validateWorkspace(Workspace workspace) {
        validateTenantId(workspace.getTenant().getId());
    }

    public void validateUser(AppUser user) {
        validateTenantId(user.getTenant().getId());
    }

    public void validateDocument(Document document) {
        validateWorkspace(document.getWorkspace());
    }

    public void validateAgent(Agent agent) {
        validateWorkspace(agent.getWorkspace());
    }

    public void validateWorkflow(Workflow workflow) {
        validateWorkspace(workflow.getWorkspace());
    }

    public void validateWorkflowRun(WorkflowRun run) {
        validateWorkflow(run.getWorkflow());
    }
}
