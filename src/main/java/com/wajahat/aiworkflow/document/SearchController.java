package com.wajahat.aiworkflow.document;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SemanticSearchService semanticSearchService;

    @PostMapping("/api/workspaces/{workspaceId}/search")
    public List<SearchResultResponse> search(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody SearchRequest request
    ) {
        return semanticSearchService.search(workspaceId, request);
    }
}