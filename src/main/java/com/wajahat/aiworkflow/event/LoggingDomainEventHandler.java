package com.wajahat.aiworkflow.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingDomainEventHandler implements DomainEventHandler {

    @Override
    public boolean supports(DomainEventType type) {
        return true;
    }

    @Override
    public void handle(DomainEvent event) {
        log.info(
                "domain_event type={} eventId={} aggregateType={} aggregateId={} payload={}",
                event.type(),
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.payload()
        );
    }
}