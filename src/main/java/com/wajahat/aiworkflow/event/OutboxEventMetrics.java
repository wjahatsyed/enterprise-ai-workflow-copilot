package com.wajahat.aiworkflow.event;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventMetrics {

    private final OutboxEventService outboxEventService;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    void registerGauges() {
        meterRegistry.gauge(
                "outbox.events.pending",
                outboxEventService,
                service -> service.countByStatus(OutboxEventStatus.PENDING)
        );
        meterRegistry.gauge(
                "outbox.events.failed",
                outboxEventService,
                service -> service.countByStatus(OutboxEventStatus.FAILED)
        );
        meterRegistry.gauge(
                "outbox.events.dead_letter",
                outboxEventService,
                service -> service.countByStatus(OutboxEventStatus.DEAD_LETTER)
        );
    }
}
