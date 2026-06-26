package com.wajahat.aiworkflow.event;

public interface DomainEventHandler {
    boolean supports(DomainEventType type);

    void handle(DomainEvent event);
}