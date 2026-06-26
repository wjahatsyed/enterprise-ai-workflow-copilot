package com.wajahat.aiworkflow.workflow;

import com.wajahat.aiworkflow.tenant.TenantContext;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import com.wajahat.aiworkflow.agent.AgentService;
import com.wajahat.aiworkflow.agent.AskAgentResponse;
import com.wajahat.aiworkflow.action.ActionDispatcher;
import com.wajahat.aiworkflow.action.ActionExecutionResult;
import com.wajahat.aiworkflow.action.ActionStepConfig;
import com.wajahat.aiworkflow.event.DomainEvent;
import com.wajahat.aiworkflow.event.DomainEventPublisher;
import com.wajahat.aiworkflow.event.DomainEventType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowRunRepository runRepository;
    private final WorkflowStepRunRepository stepRunRepository;
    private final ObjectMapper objectMapper;
    private final AgentService agentService;
    private final ActionDispatcher actionDispatcher;
    private final DomainEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    @Transactional
    public WorkflowResponse create(UUID workspaceId, CreateWorkflowRequest request) {
        Workspace workspace = workspaceRepository.findByIdAndTenantId(workspaceId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        Workflow workflow = new Workflow();
        workflow.setWorkspace(workspace);
        workflow.setName(request.name());
        workflow.setDescription(request.description());
        workflow.setStatus(WorkflowStatus.ACTIVE);

        Workflow savedWorkflow = workflowRepository.save(workflow);

        for (int i = 0; i < request.steps().size(); i++) {
            WorkflowStepRequest stepRequest = request.steps().get(i);

            WorkflowStep step = new WorkflowStep();
            step.setWorkflow(savedWorkflow);
            step.setStepOrder(i + 1);
            step.setName(stepRequest.name());
            step.setType(stepRequest.type());
            step.setConfigJson(stepRequest.configJson());

            stepRepository.save(step);
        }

        return findById(savedWorkflow.getId());
    }

    public List<WorkflowResponse> findByWorkspace(UUID workspaceId) {
        return workflowRepository.findByWorkspaceIdAndWorkspaceTenantId(workspaceId, TenantContext.getTenantId())
                .stream()
                .map(workflow -> findById(workflow.getId()))
                .toList();
    }

    public WorkflowResponse findById(UUID workflowId) {
        Workflow workflow = workflowRepository.findByIdAndWorkspaceTenantId(workflowId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        List<WorkflowStepResponse> steps = stepRepository
                .findByWorkflowIdOrderByStepOrderAsc(workflowId)
                .stream()
                .map(this::toStepResponse)
                .toList();

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getWorkspace().getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                steps
        );
    }

    @Transactional
    public WorkflowRunResponse startRun(UUID workflowId, StartWorkflowRunRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Workflow workflow = workflowRepository.findByIdAndWorkspaceTenantId(workflowId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        WorkflowRun run = new WorkflowRun();
        run.setWorkflow(workflow);
        run.setStatus(WorkflowRunStatus.RUNNING);
        run.setInputJson(request.inputJson());

        WorkflowRun savedRun = runRepository.save(run);

        eventPublisher.publish(DomainEvent.of(
                DomainEventType.WORKFLOW_STARTED,
                savedRun.getId(),
                "WorkflowRun",
                Map.of(
                        "workflowId", workflow.getId().toString(),
                        "workflowName", workflow.getName()
                )
        ));

        meterRegistry.counter("workflow.runs.started", "workflowId", workflow.getId().toString()).increment();

        List<WorkflowStep> steps = stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId);
        WorkflowRunResponse response = continueRun(savedRun, steps, request.inputJson());
        sample.stop(meterRegistry.timer("workflow.run.duration", "workflowId", workflow.getId().toString()));
        return response;
    }

    private WorkflowRunResponse continueRun(
            WorkflowRun savedRun,
            List<WorkflowStep> steps,
            String currentInput
    ) {
        String finalOutput = currentInput;
        for (WorkflowStep step : steps) {
            WorkflowStepRun stepRun = new WorkflowStepRun();
            stepRun.setWorkflowRun(savedRun);
            stepRun.setWorkflowStep(step);
            stepRun.setStatus(WorkflowRunStatus.RUNNING);
            stepRun.setInputJson(currentInput);
            stepRun.setStartedAt(LocalDateTime.now());

            try {
                String output = executeStep(step, currentInput);
                stepRun.setOutputJson(output);
                stepRun.setStatus(resolveCompletedStatus(step));
                stepRun.setCompletedAt(LocalDateTime.now());

                stepRunRepository.save(stepRun);

                eventPublisher.publish(DomainEvent.of(
                        DomainEventType.WORKFLOW_STEP_COMPLETED,
                        savedRun.getId(),
                        "WorkflowRun",
                        Map.of(
                                "stepId", step.getId().toString(),
                                "stepName", step.getName(),
                                "stepType", step.getType().name(),
                                "status", stepRun.getStatus().name()
                        )
                ));

                if (step.getType() == WorkflowStepType.HUMAN_APPROVAL) {
                    savedRun.setApprovalStatus(ApprovalStatus.PENDING);
                    savedRun.setStatus(WorkflowRunStatus.WAITING_FOR_APPROVAL);
                    savedRun.setOutputJson(output);
                    runRepository.save(savedRun);

                    eventPublisher.publish(DomainEvent.of(
                            DomainEventType.APPROVAL_REQUESTED,
                            savedRun.getId(),
                            "WorkflowRun",
                            Map.of(
                                    "workflowId", savedRun.getWorkflow().getId().toString(),
                                    "stepId", step.getId().toString(),
                                    "stepName", step.getName()
                            )
                    ));

                    return findRunById(savedRun.getId());
                }

                currentInput = output;
                finalOutput = output;
            } catch (Exception e) {
                stepRun.setStatus(WorkflowRunStatus.FAILED);
                stepRun.setErrorMessage(e.getMessage());
                stepRun.setCompletedAt(LocalDateTime.now());
                stepRunRepository.save(stepRun);

                eventPublisher.publish(DomainEvent.of(
                        DomainEventType.WORKFLOW_STEP_COMPLETED,
                        savedRun.getId(),
                        "WorkflowRun",
                        Map.of(
                                "stepId", step.getId().toString(),
                                "stepName", step.getName(),
                                "stepType", step.getType().name(),
                                "status", stepRun.getStatus().name()
                        )
                ));

                savedRun.setStatus(WorkflowRunStatus.FAILED);
                savedRun.setOutputJson(finalOutput);
                runRepository.save(savedRun);

                return findRunById(savedRun.getId());
            }
        }

        savedRun.setStatus(WorkflowRunStatus.COMPLETED);
        savedRun.setOutputJson(finalOutput);
        runRepository.save(savedRun);

        return findRunById(savedRun.getId());
    }

    public WorkflowRunResponse findRunById(UUID runId) {
        WorkflowRun run = runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Workflow run not found"));

        List<WorkflowStepRunResponse> stepRuns = stepRunRepository
                .findByWorkflowRunIdOrderByCreatedAtAsc(runId)
                .stream()
                .map(this::toStepRunResponse)
                .toList();

        return new WorkflowRunResponse(
                run.getId(),
                run.getWorkflow().getId(),
                run.getStatus(),
                run.getInputJson(),
                run.getOutputJson(),
                stepRuns
        );
    }

    private String executeStep(WorkflowStep step, String inputJson) {
        return switch (step.getType()) {
            case MANUAL_TASK -> """
                {"status":"manual_task_recorded","input":%s}
                """.formatted(inputJson);

            case HUMAN_APPROVAL -> """
                {"status":"waiting_for_human_approval","input":%s}
                """.formatted(inputJson);

            case AI_AGENT -> executeAiAgentStep(step, inputJson);

            case EXTERNAL_ACTION -> executeExternalActionStep(step, inputJson);
        };
    }

    private String executeAiAgentStep(WorkflowStep step, String inputJson) {
        try {
            AiAgentStepConfig config =
                    objectMapper.readValue(step.getConfigJson(), AiAgentStepConfig.class);

            if (config.agentId() == null) {
                throw new IllegalArgumentException("AI_AGENT step requires agentId in configJson");
            }

            String question = buildAgentQuestion(config.promptTemplate(), inputJson);

            AskAgentResponse response = agentService.askFromWorkflow(
                    config.agentId(),
                    question
            );

            return """
                {"status":"ai_agent_completed","conversationId":"%s","answer":%s}
                """.formatted(
                    response.conversationId(),
                    objectMapper.writeValueAsString(response.answer())
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute AI agent step: " + e.getMessage(), e);
        }
    }

    private String executeExternalActionStep(WorkflowStep step, String inputJson) {
        try {
            ActionStepConfig config =
                    objectMapper.readValue(step.getConfigJson(), ActionStepConfig.class);

            if (config.actionType() == null) {
                throw new IllegalArgumentException("EXTERNAL_ACTION step requires actionType in configJson");
            }

            ActionExecutionResult result = actionDispatcher.execute(config, inputJson);

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute external action step: " + e.getMessage(), e);
        }
    }

    private String buildAgentQuestion(String promptTemplate, String inputJson) {
        if (promptTemplate == null || promptTemplate.isBlank()) {
            return "Analyze the following workflow input and provide the next best action:\n" + inputJson;
        }

        return promptTemplate.replace("{{input}}", inputJson);
    }

    private WorkflowRunStatus resolveCompletedStatus(WorkflowStep step) {
        if (step.getType() == WorkflowStepType.HUMAN_APPROVAL) {
            return WorkflowRunStatus.WAITING_FOR_APPROVAL;
        }
        return WorkflowRunStatus.COMPLETED;
    }

    private WorkflowStepResponse toStepResponse(WorkflowStep step) {
        return new WorkflowStepResponse(
                step.getId(),
                step.getStepOrder(),
                step.getName(),
                step.getType(),
                step.getConfigJson()
        );
    }

    private WorkflowStepRunResponse toStepRunResponse(WorkflowStepRun stepRun) {
        return new WorkflowStepRunResponse(
                stepRun.getId(),
                stepRun.getWorkflowStep().getId(),
                stepRun.getWorkflowStep().getName(),
                stepRun.getWorkflowStep().getType(),
                stepRun.getStatus(),
                stepRun.getInputJson(),
                stepRun.getOutputJson(),
                stepRun.getErrorMessage(),
                stepRun.getStartedAt(),
                stepRun.getCompletedAt()
        );
    }

    @Transactional
    public ApprovalResponse approveRun(
            UUID runId,
            ApproveWorkflowRequest request) {

        WorkflowRun run = runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Run not found"));

        if (run.getStatus() != WorkflowRunStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("Run is not waiting for approval");
        }

        run.setApprovalStatus(ApprovalStatus.APPROVED);
        run.setApprovedBy(request.approvedBy());
        run.setApprovedAt(LocalDateTime.now());

        run.setStatus(WorkflowRunStatus.RUNNING);

        runRepository.save(run);

        eventPublisher.publish(DomainEvent.of(
                DomainEventType.APPROVAL_APPROVED,
                run.getId(),
                "WorkflowRun",
                Map.of(
                        "approvedBy", request.approvedBy()
                )
        ));

        WorkflowStepRun approvalStepRun = findLastStepRun(run);
        if (approvalStepRun.getWorkflowStep().getType() != WorkflowStepType.HUMAN_APPROVAL) {
            throw new IllegalStateException("Last workflow step run is not waiting for approval");
        }

        List<WorkflowStep> remainingSteps = stepRepository
                .findByWorkflowIdOrderByStepOrderAsc(run.getWorkflow().getId())
                .stream()
                .filter(step -> step.getStepOrder() > approvalStepRun.getWorkflowStep().getStepOrder())
                .toList();

        continueRun(run, remainingSteps, run.getOutputJson());

        return toApprovalResponse(run);
    }

    private WorkflowStepRun findLastStepRun(WorkflowRun run) {
        List<WorkflowStepRun> stepRuns = stepRunRepository
                .findByWorkflowRunIdOrderByCreatedAtAsc(run.getId());

        if (stepRuns.isEmpty()) {
            throw new IllegalStateException("No workflow step runs found for approval");
        }

        return stepRuns.get(stepRuns.size() - 1);
    }

    @Transactional
    public ApprovalResponse rejectRun(
            UUID runId,
            RejectWorkflowRequest request) {

        WorkflowRun run = runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Run not found"));

        if (run.getStatus() != WorkflowRunStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("Run is not waiting for approval");
        }

        run.setApprovalStatus(ApprovalStatus.REJECTED);
        run.setApprovedBy(request.rejectedBy());
        run.setApprovedAt(LocalDateTime.now());
        run.setRejectionReason(request.reason());

        run.setStatus(WorkflowRunStatus.FAILED);

        runRepository.save(run);

        eventPublisher.publish(DomainEvent.of(
                DomainEventType.APPROVAL_REJECTED,
                run.getId(),
                "WorkflowRun",
                Map.of(
                        "rejectedBy", request.rejectedBy(),
                        "reason", request.reason()
                )
        ));

        return toApprovalResponse(run);
    }

    public ApprovalResponse getApproval(UUID runId) {

        WorkflowRun run = runRepository.findByIdAndWorkflowWorkspaceTenantId(runId, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Run not found"));

        return toApprovalResponse(run);
    }

    private ApprovalResponse toApprovalResponse(WorkflowRun run) {
        return new ApprovalResponse(
                run.getId(),
                run.getApprovalStatus(),
                run.getApprovedBy(),
                run.getApprovedAt(),
                run.getRejectionReason()
        );
    }
}

