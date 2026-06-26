package com.wajahat.aiworkflow.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
