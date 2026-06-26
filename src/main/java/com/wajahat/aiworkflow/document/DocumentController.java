package com.wajahat.aiworkflow.document;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/api/workspaces/{workspaceId}/documents")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public DocumentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return documentService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/documents")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public List<DocumentResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return documentService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/documents/{documentId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public DocumentDetailResponse findById(@PathVariable UUID documentId) {
        return documentService.findById(documentId);
    }
}
