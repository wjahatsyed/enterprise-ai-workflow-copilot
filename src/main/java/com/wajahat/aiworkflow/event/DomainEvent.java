package com.wajahat.aiworkflow.event;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        DomainEventType type,
        UUID aggregateId,
        String aggregateType,
        Map<String, Object> payload,
        LocalDateTime occurredAt
) {
    public static DomainEvent of(
            DomainEventType type,
            UUID aggregateId,
            String aggregateType,
            Map<String, Object> payload
    ) {
        return new DomainEvent(
                UUID.randomUUID(),
                type,
                aggregateId,
                aggregateType,
                payload,
                LocalDateTime.now()
        );
    }
}