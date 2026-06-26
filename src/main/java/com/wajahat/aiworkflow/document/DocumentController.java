package com.wajahat.aiworkflow.document;

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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documents", description = "Workspace document ingestion and retrieval")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/api/workspaces/{workspaceId}/documents")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Create document", description = "Creates a document in a workspace. Requires TENANT_ADMIN.")
    public DocumentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return documentService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/documents")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "List workspace documents", description = "Lists documents in a workspace.")
    public List<DocumentResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return documentService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/documents/{documentId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Get document", description = "Returns document details and chunks.")
    public DocumentDetailResponse findById(@PathVariable UUID documentId) {
        return documentService.findById(documentId);
    }
}
