package com.wajahat.aiworkflow.workflow;

import com.wajahat.aiworkflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_runs")
public class WorkflowRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowRunStatus status;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String inputJson;

    @Column(columnDefinition = "jsonb")
    private String outputJson;
}