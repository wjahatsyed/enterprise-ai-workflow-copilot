package com.wajahat.aiworkflow.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepRunRepository extends JpaRepository<WorkflowStepRun, UUID> {
    List<WorkflowStepRun> findByWorkflowRunIdOrderByCreatedAtAsc(UUID workflowRunId);
}