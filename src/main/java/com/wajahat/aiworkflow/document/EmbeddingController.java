package com.wajahat.aiworkflow.document;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmbeddingController {

    private final DocumentEmbeddingService embeddingService;

    @PostMapping("/api/documents/{documentId}/embeddings")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public String embed(@PathVariable UUID documentId) {
        int count = embeddingService.embedDocument(documentId);
        return "Embedded chunks: " + count;
    }
}
