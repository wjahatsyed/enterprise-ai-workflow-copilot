package com.wajahat.aiworkflow.document;

import com.wajahat.aiworkflow.common.BaseEntity;
import com.wajahat.aiworkflow.workspace.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;
}