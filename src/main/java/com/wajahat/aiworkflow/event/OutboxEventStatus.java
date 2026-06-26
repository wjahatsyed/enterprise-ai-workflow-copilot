package com.wajahat.aiworkflow.event;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
