package com.wajahat.aiworkflow.workspace;

import com.wajahat.aiworkflow.exception.TenantMismatchException;
import com.wajahat.aiworkflow.tenant.Tenant;
import com.wajahat.aiworkflow.tenant.TenantContext;
import com.wajahat.aiworkflow.tenant.TenantRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final TenantRepository tenantRepository;

    public WorkspaceResponse create(UUID tenantId, CreateWorkspaceRequest request) {
        validateTenant(tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Workspace workspace = new Workspace();
        workspace.setTenant(tenant);
        workspace.setName(request.name());
        workspace.setDescription(request.description());

        return toResponse(workspaceRepository.save(workspace));
    }

    public List<WorkspaceResponse> findByTenant(UUID tenantId) {
        validateTenant(tenantId);
        return workspaceRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateTenant(UUID tenantId) {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId != null && !currentTenantId.equals(tenantId)) {
            throw new TenantMismatchException("Access denied: tenant mismatch");
        }
    }

    private WorkspaceResponse toResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getTenant().getId(),
                workspace.getName(),
                workspace.getDescription()
        );
    }
}