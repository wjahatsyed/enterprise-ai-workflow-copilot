package com.wajahat.aiworkflow.workflow;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, UUID> {
    Optional<WorkflowRun> findByIdAndWorkflowWorkspaceTenantId(UUID id, UUID tenantId);
}