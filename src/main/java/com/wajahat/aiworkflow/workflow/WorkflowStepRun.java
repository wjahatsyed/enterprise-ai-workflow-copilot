package com.wajahat.aiworkflow.workflow;

import com.wajahat.aiworkflow.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_step_runs")
public class WorkflowStepRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_run_id", nullable = false)
    private WorkflowRun workflowRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowRunStatus status;

    @Column(columnDefinition = "jsonb")
    private String inputJson;

    @Column(columnDefinition = "jsonb")
    private String outputJson;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}