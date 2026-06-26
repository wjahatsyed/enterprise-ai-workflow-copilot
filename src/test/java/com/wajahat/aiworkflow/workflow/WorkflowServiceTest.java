package com.wajahat.aiworkflow.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wajahat.aiworkflow.action.ActionDispatcher;
import com.wajahat.aiworkflow.action.ActionExecutionResult;
import com.wajahat.aiworkflow.action.ActionType;
import com.wajahat.aiworkflow.agent.AgentService;
import com.wajahat.aiworkflow.agent.AskAgentResponse;
import com.wajahat.aiworkflow.event.DomainEventPublisher;
import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.tenant.TenantContext;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStepRepository stepRepository;

    @Mock
    private WorkflowRunRepository runRepository;

    @Mock
    private WorkflowStepRunRepository stepRunRepository;

    @Mock
    private AgentService agentService;

    @Mock
    private ActionDispatcher actionDispatcher;

    @Mock
    private DomainEventPublisher eventPublisher;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        workflowService = new WorkflowService(
                workspaceRepository,
                workflowRepository,
                stepRepository,
                runRepository,
                stepRunRepository,
                new ObjectMapper(),
                agentService,
                actionDispatcher,
                eventPublisher
        );
    }

    @Test
    void approveRunShouldContinueRemainingStepsAfterHumanApproval() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(tenantId);
        WorkflowRun run = waitingForApprovalRun(runId, workflow);
        WorkflowStep aiStep = step(workflow, 1, WorkflowStepType.AI_AGENT, "{}");
        WorkflowStep approvalStep = step(workflow, 2, WorkflowStepType.HUMAN_APPROVAL, "{}");
        WorkflowStep actionStep = step(
                workflow,
                3,
                WorkflowStepType.EXTERNAL_ACTION,
                "{\"actionType\":\"WEBHOOK\",\"parameters\":{}}"
        );
        WorkflowStepRun aiStepRun = completedStepRun(run, aiStep, "{\"status\":\"ai_agent_completed\"}");
        WorkflowStepRun approvalStepRun = completedStepRun(
                run,
                approvalStep,
                "{\"status\":\"waiting_for_human_approval\"}"
        );

        when(runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, tenantId)).thenReturn(Optional.of(run));
        when(stepRunRepository.findByWorkflowRunIdOrderByCreatedAtAsc(runId))
                .thenReturn(List.of(aiStepRun, approvalStepRun));
        when(stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflow.getId()))
                .thenReturn(List.of(aiStep, approvalStep, actionStep));
        when(actionDispatcher.execute(any(), any()))
                .thenReturn(new ActionExecutionResult(ActionType.WEBHOOK, true, "sent", Map.of()));

        ApprovalResponse response = workflowService.approveRun(
                runId,
                new ApproveWorkflowRequest("reviewer@example.com")
        );

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(run.getStatus()).isEqualTo(WorkflowRunStatus.COMPLETED);
        assertThat(run.getOutputJson()).contains("\"actionType\":\"WEBHOOK\"");
        verify(actionDispatcher).execute(any(), any());
        verify(stepRunRepository).save(any(WorkflowStepRun.class));
    }

    @Test
    void approveRunShouldWaitAgainWhenNextRemainingStepRequiresApproval() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(tenantId);
        WorkflowRun run = waitingForApprovalRun(runId, workflow);
        WorkflowStep firstApprovalStep = step(workflow, 1, WorkflowStepType.HUMAN_APPROVAL, "{}");
        WorkflowStep secondApprovalStep = step(workflow, 2, WorkflowStepType.HUMAN_APPROVAL, "{}");
        WorkflowStepRun approvalStepRun = completedStepRun(
                run,
                firstApprovalStep,
                "{\"status\":\"waiting_for_human_approval\"}"
        );

        when(runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, tenantId)).thenReturn(Optional.of(run));
        when(stepRunRepository.findByWorkflowRunIdOrderByCreatedAtAsc(runId))
                .thenReturn(List.of(approvalStepRun));
        when(stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflow.getId()))
                .thenReturn(List.of(firstApprovalStep, secondApprovalStep));

        ApprovalResponse response = workflowService.approveRun(
                runId,
                new ApproveWorkflowRequest("reviewer@example.com")
        );

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(run.getStatus()).isEqualTo(WorkflowRunStatus.WAITING_FOR_APPROVAL);
        verify(actionDispatcher, never()).execute(any(), any());
    }

    @Test
    void rejectRunShouldRemainFailedAndNotContinueWorkflow() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        UUID runId = UUID.randomUUID();
        WorkflowRun run = waitingForApprovalRun(runId, workflow(tenantId));

        when(runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, tenantId)).thenReturn(Optional.of(run));

        ApprovalResponse response = workflowService.rejectRun(
                runId,
                new RejectWorkflowRequest("reviewer@example.com", "Needs changes")
        );

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(run.getStatus()).isEqualTo(WorkflowRunStatus.FAILED);
        verify(stepRepository, never()).findByWorkflowIdOrderByStepOrderAsc(any());
    }

    private Workflow workflow(UUID tenantId) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        Workspace workspace = new Workspace();
        workspace.setTenant(tenant);
        Workflow workflow = new Workflow();
        workflow.setWorkspace(workspace);
        workflow.setName("Contract review");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        return workflow;
    }

    private WorkflowRun waitingForApprovalRun(UUID runId, Workflow workflow) {
        WorkflowRun run = new WorkflowRun();
        run.setId(runId);
        run.setWorkflow(workflow);
        run.setStatus(WorkflowRunStatus.WAITING_FOR_APPROVAL);
        run.setApprovalStatus(ApprovalStatus.PENDING);
        run.setInputJson("{\"input\":true}");
        run.setOutputJson("{\"status\":\"waiting_for_human_approval\"}");
        return run;
    }

    private WorkflowStep step(
            Workflow workflow,
            int stepOrder,
            WorkflowStepType type,
            String configJson
    ) {
        WorkflowStep step = new WorkflowStep();
        step.setWorkflow(workflow);
        step.setStepOrder(stepOrder);
        step.setName(type.name());
        step.setType(type);
        step.setConfigJson(configJson);
        return step;
    }

    private WorkflowStepRun completedStepRun(
            WorkflowRun run,
            WorkflowStep step,
            String outputJson
    ) {
        WorkflowStepRun stepRun = new WorkflowStepRun();
        stepRun.setWorkflowRun(run);
        stepRun.setWorkflowStep(step);
        stepRun.setStatus(WorkflowRunStatus.COMPLETED);
        stepRun.setInputJson(run.getInputJson());
        stepRun.setOutputJson(outputJson);
        return stepRun;
    }
}
