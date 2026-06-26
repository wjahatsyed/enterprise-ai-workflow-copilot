package com.wajahat.aiworkflow.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final List<DomainEventHandler> handlers;

    @Async("domainEventExecutor")
    @EventListener
    public void handle(DomainEvent event) {
        handlers.stream()
                .filter(handler -> handler.supports(event.type()))
                .forEach(handler -> handler.handle(event));
    }
}
