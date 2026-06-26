package com.wajahat.aiworkflow.workspace;

import com.wajahat.aiworkflow.tenant.TenantAccessValidator;
import com.wajahat.aiworkflow.user.AppUser;
import com.wajahat.aiworkflow.user.AppUserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final AppUserRepository appUserRepository;
    private final TenantAccessValidator tenantAccessValidator;

    public WorkspaceMemberResponse addMember(UUID workspaceId, AddWorkspaceMemberRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        tenantAccessValidator.validateWorkspace(workspace);

        AppUser user = appUserRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        tenantAccessValidator.validateUser(user);

        if (!user.getTenant().getId().equals(workspace.getTenant().getId())) {
            throw new IllegalArgumentException("User does not belong to workspace tenant");
        }

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.userId())) {
            throw new IllegalArgumentException("User already exists in workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(request.role());

        return toResponse(memberRepository.save(member));
    }

    public List<WorkspaceMemberResponse> findByWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        tenantAccessValidator.validateWorkspace(workspace);

        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private WorkspaceMemberResponse toResponse(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getWorkspace().getId(),
                member.getUser().getId(),
                member.getUser().getFullName(),
                member.getUser().getEmail(),
                member.getRole()
        );
    }
}
