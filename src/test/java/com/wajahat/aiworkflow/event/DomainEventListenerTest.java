package com.wajahat.aiworkflow.event;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventListenerTest {

    @Test
    void handleShouldDispatchOnlyToMatchingHandlers() {
        DomainEventHandler matchingHandler = org.mockito.Mockito.mock(DomainEventHandler.class);
        DomainEventHandler nonMatchingHandler = org.mockito.Mockito.mock(DomainEventHandler.class);
        DomainEvent event = DomainEvent.of(
                DomainEventType.APPROVAL_REQUESTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of()
        );

        when(matchingHandler.supports(event.type())).thenReturn(true);
        when(nonMatchingHandler.supports(event.type())).thenReturn(false);

        DomainEventListener listener = new DomainEventListener(List.of(matchingHandler, nonMatchingHandler));

        listener.handle(event);

        verify(matchingHandler).handle(event);
        verify(nonMatchingHandler, never()).handle(event);
    }
}
