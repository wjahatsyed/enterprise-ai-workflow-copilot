package com.wajahat.aiworkflow.document;

import com.wajahat.aiworkflow.ai.OpenAiEmbeddingClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final OpenAiEmbeddingClient embeddingClient;

    @Value("${openai.embedding-model}")
    private String embeddingModel;

    @Transactional
    public int embedDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new IllegalArgumentException("Document not found");
        }

        List<DocumentChunk> chunks =
                chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);

        int embedded = 0;

        for (DocumentChunk chunk : chunks) {
            List<Double> embedding = embeddingClient.embed(chunk.getContent());
            chunk.setEmbedding(toJson(embedding));
            chunk.setEmbeddingModel(embeddingModel);
            chunk.setEmbeddedAt(LocalDateTime.now());
            chunkRepository.save(chunk);
            embedded++;
        }

        return embedded;
    }

    private String toJson(List<Double> embedding) {
        if (embedding == null) {
            return null;
        }
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}