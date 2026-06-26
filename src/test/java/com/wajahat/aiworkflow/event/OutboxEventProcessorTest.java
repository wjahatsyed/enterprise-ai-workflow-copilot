package com.wajahat.aiworkflow.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class OutboxEventProcessorTest {

    @Test
    void processPendingEventsShouldPublishStoredDomainEvents() {
        OutboxEventService outboxEventService = org.mockito.Mockito.mock(OutboxEventService.class);
        ApplicationEventPublisher applicationEventPublisher =
                org.mockito.Mockito.mock(ApplicationEventPublisher.class);
        OutboxEventProcessor processor =
                new OutboxEventProcessor(outboxEventService, applicationEventPublisher);
        OutboxEvent outboxEvent = outboxEvent();
        DomainEvent domainEvent = DomainEvent.of(
                DomainEventType.WORKFLOW_STARTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of()
        );

        when(outboxEventService.findReadyEvents(50)).thenReturn(List.of(outboxEvent));
        when(outboxEventService.toDomainEvent(outboxEvent)).thenReturn(domainEvent);

        processor.processPendingEvents();

        verify(outboxEventService).markProcessing(outboxEvent);
        verify(applicationEventPublisher).publishEvent(domainEvent);
        verify(outboxEventService).markProcessed(outboxEvent);
    }

    @Test
    void processPendingEventsShouldMarkEventFailedWhenPublishingFails() {
        OutboxEventService outboxEventService = org.mockito.Mockito.mock(OutboxEventService.class);
        ApplicationEventPublisher applicationEventPublisher =
                org.mockito.Mockito.mock(ApplicationEventPublisher.class);
        OutboxEventProcessor processor =
                new OutboxEventProcessor(outboxEventService, applicationEventPublisher);
        OutboxEvent outboxEvent = outboxEvent();
        IllegalStateException exception = new IllegalStateException("bad payload");

        when(outboxEventService.findReadyEvents(50)).thenReturn(List.of(outboxEvent));
        when(outboxEventService.toDomainEvent(outboxEvent)).thenThrow(exception);

        processor.processPendingEvents();

        verify(outboxEventService).markProcessing(outboxEvent);
        verify(outboxEventService).markFailed(outboxEvent, exception);
    }

    private OutboxEvent outboxEvent() {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(UUID.randomUUID());
        outboxEvent.setType(DomainEventType.WORKFLOW_STARTED);
        return outboxEvent;
    }
}
