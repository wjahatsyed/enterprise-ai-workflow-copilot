package com.wajahat.aiworkflow.workflow;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/api/workspaces/{workspaceId}/workflows")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public WorkflowResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkflowRequest request
    ) {
        return workflowService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/workflows")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public List<WorkflowResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return workflowService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/workflows/{workflowId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public WorkflowResponse findById(@PathVariable UUID workflowId) {
        return workflowService.findById(workflowId);
    }

    @PostMapping("/api/workflows/{workflowId}/runs")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public WorkflowRunResponse startRun(
            @PathVariable UUID workflowId,
            @Valid @RequestBody StartWorkflowRunRequest request
    ) {
        return workflowService.startRun(workflowId, request);
    }

    @GetMapping("/api/workflow-runs/{runId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public WorkflowRunResponse findRunById(@PathVariable UUID runId) {
        return workflowService.findRunById(runId);
    }

    @GetMapping("/api/workflow-runs/{runId}/approval")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public ApprovalResponse getApproval(
            @PathVariable UUID runId) {

        return workflowService.getApproval(runId);
    }

    @PostMapping("/api/workflow-runs/{runId}/approve")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ApprovalResponse approve(
            @PathVariable UUID runId,
            @Valid @RequestBody ApproveWorkflowRequest request) {

        return workflowService.approveRun(runId, request);
    }

    @PostMapping("/api/workflow-runs/{runId}/reject")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ApprovalResponse reject(
            @PathVariable UUID runId,
            @Valid @RequestBody RejectWorkflowRequest request) {

        return workflowService.rejectRun(runId, request);
    }
}
