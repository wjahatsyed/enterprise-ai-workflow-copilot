package com.wajahat.aiworkflow.workflow;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/api/workspaces/{workspaceId}/workflows")
    public WorkflowResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkflowRequest request
    ) {
        return workflowService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/workflows")
    public List<WorkflowResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return workflowService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/workflows/{workflowId}")
    public WorkflowResponse findById(@PathVariable UUID workflowId) {
        return workflowService.findById(workflowId);
    }

    @PostMapping("/api/workflows/{workflowId}/runs")
    public WorkflowRunResponse startRun(
            @PathVariable UUID workflowId,
            @Valid @RequestBody StartWorkflowRunRequest request
    ) {
        return workflowService.startRun(workflowId, request);
    }

    @GetMapping("/api/workflow-runs/{runId}")
    public WorkflowRunResponse findRunById(@PathVariable UUID runId) {
        return workflowService.findRunById(runId);
    }
}