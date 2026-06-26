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
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Search", description = "Semantic document search")
public class SearchController {

    private final SemanticSearchService semanticSearchService;

    @PostMapping("/api/workspaces/{workspaceId}/search")
    @Operation(summary = "Search workspace documents", description = "Runs semantic search across workspace document chunks.")
    public List<SearchResultResponse> search(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody SearchRequest request
    ) {
        return semanticSearchService.search(workspaceId, request);
    }
}
