package com.wajahat.aiworkflow.event;

import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventPublisherTest {

    @Test
    void publishShouldSaveEventToOutbox() {
        OutboxEventService outboxEventService = org.mockito.Mockito.mock(OutboxEventService.class);
        DomainEventPublisher publisher = new DomainEventPublisher(outboxEventService);
        DomainEvent event = DomainEvent.of(
                DomainEventType.WORKFLOW_STARTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of()
        );

        publisher.publish(event);

        verify(outboxEventService).save(event);
    }
}
