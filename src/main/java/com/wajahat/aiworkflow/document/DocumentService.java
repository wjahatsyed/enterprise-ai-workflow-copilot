package com.wajahat.aiworkflow.document;

import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final WorkspaceRepository workspaceRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextChunker textChunker;

    @Transactional
    public DocumentResponse create(UUID workspaceId, CreateDocumentRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        Document document = new Document();
        document.setWorkspace(workspace);
        document.setTitle(request.title());
        document.setSourceType(DocumentSourceType.MANUAL_TEXT);
        document.setStatus(DocumentStatus.PROCESSING);

        Document savedDocument = documentRepository.save(document);

        List<String> chunks = textChunker.chunk(request.content());

        for (int i = 0; i < chunks.size(); i++) {
            String content = chunks.get(i);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(savedDocument);
            chunk.setChunkIndex(i);
            chunk.setContent(content);
            chunk.setTokenEstimate(textChunker.estimateTokens(content));

            chunkRepository.save(chunk);
        }

        savedDocument.setStatus(DocumentStatus.READY);
        Document updatedDocument = documentRepository.save(savedDocument);

        return toResponse(updatedDocument, chunks.size());
    }

    public List<DocumentResponse> findByWorkspace(UUID workspaceId) {
        return documentRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(document -> {
                    int chunkCount = chunkRepository
                            .findByDocumentIdOrderByChunkIndexAsc(document.getId())
                            .size();

                    return toResponse(document, chunkCount);
                })
                .toList();
    }

    public DocumentDetailResponse findById(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        List<DocumentChunkResponse> chunks = chunkRepository
                .findByDocumentIdOrderByChunkIndexAsc(documentId)
                .stream()
                .map(this::toChunkResponse)
                .toList();

        return new DocumentDetailResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getTitle(),
                document.getSourceType(),
                document.getStatus(),
                chunks
        );
    }

    private DocumentResponse toResponse(Document document, int chunkCount) {
        return new DocumentResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getTitle(),
                document.getSourceType(),
                document.getStatus(),
                chunkCount
        );
    }

    private DocumentChunkResponse toChunkResponse(DocumentChunk chunk) {
        return new DocumentChunkResponse(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                chunk.getTokenEstimate()
        );
    }
}