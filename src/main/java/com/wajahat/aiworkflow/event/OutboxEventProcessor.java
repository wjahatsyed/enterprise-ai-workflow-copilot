package com.wajahat.aiworkflow.event;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private static final int BATCH_SIZE = 50;

    private final OutboxEventService outboxEventService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${outbox.processor.fixed-delay-ms:5000}")
    public void processPendingEvents() {
        outboxEventService.findReadyEvents(BATCH_SIZE)
                .forEach(this::process);
    }

    private void process(OutboxEvent outboxEvent) {
        try {
            outboxEventService.markProcessing(outboxEvent);
            applicationEventPublisher.publishEvent(outboxEventService.toDomainEvent(outboxEvent));
            outboxEventService.markProcessed(outboxEvent);
            meterRegistry.counter("outbox.events.processed.total").increment();
        } catch (Exception e) {
            log.warn(
                    "outbox_event_failed id={} eventId={} type={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventId(),
                    outboxEvent.getType(),
                    e
            );
            outboxEventService.markFailed(outboxEvent, e);
            meterRegistry.counter("outbox.events.failed.total").increment();
        }
    }
}
