package com.wajahat.aiworkflow.workflow;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    List<Workflow> findByWorkspaceId(UUID workspaceId);
    Optional<Workflow> findByIdAndWorkspaceTenantId(UUID id, UUID tenantId);
    List<Workflow> findByWorkspaceIdAndWorkspaceTenantId(UUID workspaceId, UUID tenantId);
}