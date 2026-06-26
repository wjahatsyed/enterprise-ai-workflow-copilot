package com.wajahat.aiworkflow.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final List<DomainEventHandler> handlers;

    public void publish(DomainEvent event) {
        handlers.stream()
                .filter(handler -> handler.supports(event.type()))
                .forEach(handler -> handler.handle(event));
    }
}