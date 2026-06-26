package com.wajahat.aiworkflow.workflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workflows", description = "Workflow definitions, runs, and approvals")
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/api/workspaces/{workspaceId}/workflows")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Create workflow", description = "Creates a workflow definition. Requires TENANT_ADMIN.")
    public WorkflowResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkflowRequest request
    ) {
        return workflowService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/workflows")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "List workspace workflows", description = "Lists workflow definitions in a workspace.")
    public List<WorkflowResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return workflowService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/workflows/{workflowId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Get workflow", description = "Returns a workflow definition with its steps.")
    public WorkflowResponse findById(@PathVariable UUID workflowId) {
        return workflowService.findById(workflowId);
    }

    @PostMapping("/api/workflows/{workflowId}/runs")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Start workflow run", description = "Starts a workflow run. Requires TENANT_ADMIN.")
    public WorkflowRunResponse startRun(
            @PathVariable UUID workflowId,
            @Valid @RequestBody StartWorkflowRunRequest request
    ) {
        return workflowService.startRun(workflowId, request);
    }

    @GetMapping("/api/workflow-runs/{runId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Get workflow run", description = "Returns workflow run status and completed step runs.")
    public WorkflowRunResponse findRunById(@PathVariable UUID runId) {
        return workflowService.findRunById(runId);
    }

    @GetMapping("/api/workflow-runs/{runId}/approval")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Get workflow approval", description = "Returns the pending or completed human approval step for a run.")
    public ApprovalResponse getApproval(
            @PathVariable UUID runId) {

        return workflowService.getApproval(runId);
    }

    @PostMapping("/api/workflow-runs/{runId}/approve")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Approve workflow run", description = "Approves a waiting workflow run and continues execution. Requires TENANT_ADMIN.")
    public ApprovalResponse approve(
            @PathVariable UUID runId,
            @Valid @RequestBody ApproveWorkflowRequest request) {

        return workflowService.approveRun(runId, request);
    }

    @PostMapping("/api/workflow-runs/{runId}/reject")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Reject workflow run", description = "Rejects a waiting workflow run and marks it failed. Requires TENANT_ADMIN.")
    public ApprovalResponse reject(
            @PathVariable UUID runId,
            @Valid @RequestBody RejectWorkflowRequest request) {

        return workflowService.rejectRun(runId, request);
    }
}
