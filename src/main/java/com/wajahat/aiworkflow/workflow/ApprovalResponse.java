package com.wajahat.aiworkflow.workflow;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalResponse(
        UUID workflowRunId,
        ApprovalStatus approvalStatus,
        String approvedBy,
        LocalDateTime approvedAt,
        String rejectionReason
) {}