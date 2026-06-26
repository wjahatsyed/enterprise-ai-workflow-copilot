package com.wajahat.aiworkflow.event;

import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class DomainEventPublisherTest {

    @Test
    void publishShouldDelegateToSpringApplicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher =
                org.mockito.Mockito.mock(ApplicationEventPublisher.class);
        DomainEventPublisher publisher = new DomainEventPublisher(applicationEventPublisher);
        DomainEvent event = DomainEvent.of(
                DomainEventType.WORKFLOW_STARTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of()
        );

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }
}
