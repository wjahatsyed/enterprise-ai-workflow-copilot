package com.wajahat.aiworkflow.document;

import com.wajahat.aiworkflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "document_chunks")
public class DocumentChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer tokenEstimate;

    @Column(columnDefinition = "jsonb")
    private String embedding;

    private String embeddingModel;

    private LocalDateTime embeddedAt;
}