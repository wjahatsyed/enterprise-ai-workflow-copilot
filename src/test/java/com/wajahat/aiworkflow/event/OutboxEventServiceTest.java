package com.wajahat.aiworkflow.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxEventServiceTest {

    @Test
    void saveShouldPersistDomainEventAsPendingOutboxEvent() {
        OutboxEventRepository repository = org.mockito.Mockito.mock(OutboxEventRepository.class);
        OutboxEventService service = new OutboxEventService(repository, new ObjectMapper());
        DomainEvent event = DomainEvent.of(
                DomainEventType.APPROVAL_REQUESTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of("stepName", "Review")
        );

        when(repository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OutboxEvent saved = service.save(event);

        assertThat(saved.getEventId()).isEqualTo(event.eventId());
        assertThat(saved.getType()).isEqualTo(event.type());
        assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getPayloadJson()).contains("Review");
        verify(repository).save(saved);
    }

    @Test
    void toDomainEventShouldRestoreStoredEvent() {
        OutboxEventRepository repository = org.mockito.Mockito.mock(OutboxEventRepository.class);
        OutboxEventService service = new OutboxEventService(repository, new ObjectMapper());
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setType(DomainEventType.WORKFLOW_STEP_COMPLETED);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setAggregateType("WorkflowRun");
        outboxEvent.setPayloadJson("{\"status\":\"COMPLETED\"}");
        outboxEvent.setOccurredAt(java.time.LocalDateTime.now());

        DomainEvent domainEvent = service.toDomainEvent(outboxEvent);

        assertThat(domainEvent.eventId()).isEqualTo(eventId);
        assertThat(domainEvent.type()).isEqualTo(DomainEventType.WORKFLOW_STEP_COMPLETED);
        assertThat(domainEvent.aggregateId()).isEqualTo(aggregateId);
        assertThat(domainEvent.payload()).containsEntry("status", "COMPLETED");
    }
}
