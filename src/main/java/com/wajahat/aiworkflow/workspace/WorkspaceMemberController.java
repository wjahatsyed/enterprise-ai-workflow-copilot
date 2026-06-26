package com.wajahat.aiworkflow.workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workspace Members", description = "Workspace membership management")
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Add workspace member", description = "Adds a user to a workspace. Requires TENANT_ADMIN.")
    public WorkspaceMemberResponse addMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddWorkspaceMemberRequest request
    ) {
        return memberService.addMember(workspaceId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "List workspace members", description = "Lists members for a workspace.")
    public List<WorkspaceMemberResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return memberService.findByWorkspace(workspaceId);
    }
}
