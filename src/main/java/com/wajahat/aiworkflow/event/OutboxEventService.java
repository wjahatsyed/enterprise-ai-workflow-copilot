package com.wajahat.aiworkflow.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {};

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${outbox.retry.max-attempts:3}")
    private int maxAttempts = 3;

    @Value("${outbox.retry.initial-backoff-ms:1000}")
    private long initialBackoffMs = 1000;

    @Value("${outbox.retry.max-backoff-ms:60000}")
    private long maxBackoffMs = 60000;

    @Transactional
    public OutboxEvent save(DomainEvent event) {
        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventId(event.eventId());
            outboxEvent.setType(event.type());
            outboxEvent.setAggregateId(event.aggregateId());
            outboxEvent.setAggregateType(event.aggregateType());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(event.payload()));
            outboxEvent.setOccurredAt(event.occurredAt());
            outboxEvent.setStatus(OutboxEventStatus.PENDING);
            outboxEvent.setRetryCount(0);

            return outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save domain event to outbox", e);
        }
    }

    public List<OutboxEvent> findReadyEvents(int batchSize) {
        return outboxEventRepository.findReadyEvents(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );
    }

    public long countByStatus(OutboxEventStatus status) {
        return outboxEventRepository.countByStatus(status);
    }

    public DomainEvent toDomainEvent(OutboxEvent outboxEvent) {
        try {
            Map<String, Object> payload =
                    objectMapper.readValue(outboxEvent.getPayloadJson(), PAYLOAD_TYPE);

            return new DomainEvent(
                    outboxEvent.getEventId(),
                    outboxEvent.getType(),
                    outboxEvent.getAggregateId(),
                    outboxEvent.getAggregateType(),
                    payload,
                    outboxEvent.getOccurredAt()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize outbox event payload", e);
        }
    }

    public void markProcessing(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxEventStatus.PROCESSING);
        outboxEvent.setNextRetryAt(null);
        outboxEvent.setErrorMessage(null);
        outboxEventRepository.save(outboxEvent);
    }

    public void markProcessed(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxEventStatus.PROCESSED);
        outboxEvent.setProcessedAt(LocalDateTime.now());
        outboxEvent.setNextRetryAt(null);
        outboxEvent.setErrorMessage(null);
        outboxEventRepository.save(outboxEvent);
    }

    public void markFailed(OutboxEvent outboxEvent, Exception exception) {
        int nextRetryCount = outboxEvent.getRetryCount() + 1;
        outboxEvent.setRetryCount(nextRetryCount);
        outboxEvent.setErrorMessage(exception.getMessage());

        if (nextRetryCount >= maxAttempts) {
            outboxEvent.setStatus(OutboxEventStatus.DEAD_LETTER);
            outboxEvent.setNextRetryAt(null);
        } else {
            outboxEvent.setStatus(OutboxEventStatus.FAILED);
            outboxEvent.setNextRetryAt(LocalDateTime.now().plusNanos(backoffMillis(nextRetryCount) * 1_000_000));
        }

        outboxEventRepository.save(outboxEvent);
    }

    private long backoffMillis(int retryCount) {
        long multiplier = 1L << Math.max(0, retryCount - 1);
        long backoff = initialBackoffMs * multiplier;
        return Math.min(backoff, maxBackoffMs);
    }
}
