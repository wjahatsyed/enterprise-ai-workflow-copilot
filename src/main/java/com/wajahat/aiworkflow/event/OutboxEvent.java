package com.wajahat.aiworkflow.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainEventType type;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payloadJson;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(nullable = false)
    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime nextRetryAt;

    private LocalDateTime processedAt;
}
