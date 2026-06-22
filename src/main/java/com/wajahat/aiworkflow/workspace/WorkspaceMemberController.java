package com.wajahat.aiworkflow.workspace;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    @PostMapping
    public WorkspaceMemberResponse addMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddWorkspaceMemberRequest request
    ) {
        return memberService.addMember(workspaceId, request);
    }

    @GetMapping
    public List<WorkspaceMemberResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return memberService.findByWorkspace(workspaceId);
    }
}