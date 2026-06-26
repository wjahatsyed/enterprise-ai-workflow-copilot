package com.wajahat.aiworkflow.document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wajahat.aiworkflow.ai.OpenAiEmbeddingClient;
import com.wajahat.aiworkflow.tenant.TenantAccessValidator;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final DocumentChunkRepository chunkRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OpenAiEmbeddingClient embeddingClient;
    private final ObjectMapper objectMapper;
    private final TenantAccessValidator tenantAccessValidator;

    public List<SearchResultResponse> search(UUID workspaceId, SearchRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        tenantAccessValidator.validateWorkspace(workspace);

        int topK = request.topK() == null ? 5 : request.topK();

        List<Double> queryEmbedding = embeddingClient.embed(request.query());

        return chunkRepository.findByDocumentWorkspaceIdAndEmbeddingIsNotNull(workspaceId)
                .parallelStream()
                .map(chunk -> toSearchResult(chunk, queryEmbedding))
                .sorted(Comparator.comparingDouble(SearchResultResponse::score).reversed())
                .limit(topK)
                .toList();
    }

    private SearchResultResponse toSearchResult(DocumentChunk chunk, List<Double> queryEmbedding) {
        List<Double> chunkEmbedding = fromJson(chunk.getEmbedding());

        return new SearchResultResponse(
                chunk.getDocument().getId(),
                chunk.getId(),
                chunk.getDocument().getTitle(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                cosineSimilarity(queryEmbedding, chunkEmbedding)
        );
    }

    private List<Double> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse embedding", e);
        }
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty() || a.size() != b.size()) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            double aVal = a.get(i);
            double bVal = b.get(i);
            dot += aVal * bVal;
            normA += aVal * aVal;
            normB += bVal * bVal;
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
