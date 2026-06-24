package com.wajahat.aiworkflow.workflow;

import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
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

    @Transactional
    public WorkflowResponse create(UUID workspaceId, CreateWorkflowRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
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
        return workflowRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(workflow -> findById(workflow.getId()))
                .toList();
    }

    public WorkflowResponse findById(UUID workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
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
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        WorkflowRun run = new WorkflowRun();
        run.setWorkflow(workflow);
        run.setStatus(WorkflowRunStatus.RUNNING);
        run.setInputJson(request.inputJson());

        WorkflowRun savedRun = runRepository.save(run);

        List<WorkflowStep> steps = stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId);

        String currentInput = request.inputJson();
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

                if (step.getType() == WorkflowStepType.HUMAN_APPROVAL) {
                    savedRun.setStatus(WorkflowRunStatus.WAITING_FOR_APPROVAL);
                    savedRun.setOutputJson(output);
                    runRepository.save(savedRun);
                    return findRunById(savedRun.getId());
                }

                currentInput = output;
                finalOutput = output;
            } catch (Exception e) {
                stepRun.setStatus(WorkflowRunStatus.FAILED);
                stepRun.setErrorMessage(e.getMessage());
                stepRun.setCompletedAt(LocalDateTime.now());
                stepRunRepository.save(stepRun);

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
        WorkflowRun run = runRepository.findById(runId)
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

            case AI_AGENT -> """
                    {"status":"ai_agent_step_placeholder","stepName":"%s","input":%s}
                    """.formatted(step.getName(), inputJson);
        };
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
}