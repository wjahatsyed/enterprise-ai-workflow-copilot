package com.wajahat.aiworkflow.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxEventService outboxEventService;

    public void publish(DomainEvent event) {
        outboxEventService.save(event);
    }
}
