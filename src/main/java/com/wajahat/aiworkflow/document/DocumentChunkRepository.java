package com.wajahat.aiworkflow.document;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

    @Query("SELECT c FROM DocumentChunk c JOIN FETCH c.document d WHERE d.workspace.id = :workspaceId AND c.embedding IS NOT NULL")
    List<DocumentChunk> findByDocumentWorkspaceIdAndEmbeddingIsNotNull(UUID workspaceId);
}