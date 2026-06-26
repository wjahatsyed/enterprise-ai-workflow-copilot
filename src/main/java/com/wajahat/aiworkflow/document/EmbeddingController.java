package com.wajahat.aiworkflow.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Embeddings", description = "Document embedding operations")
public class EmbeddingController {

    private final DocumentEmbeddingService embeddingService;

    @PostMapping("/api/documents/{documentId}/embeddings")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Embed document", description = "Generates embeddings for a document. Requires TENANT_ADMIN.")
    public String embed(@PathVariable UUID documentId) {
        int count = embeddingService.embedDocument(documentId);
        return "Embedded chunks: " + count;
    }
}
