package com.wajahat.aiworkflow.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {};

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

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
                PageRequest.of(0, batchSize)
        );
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
        outboxEvent.setErrorMessage(null);
        outboxEventRepository.save(outboxEvent);
    }

    public void markProcessed(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxEventStatus.PROCESSED);
        outboxEvent.setProcessedAt(LocalDateTime.now());
        outboxEvent.setErrorMessage(null);
        outboxEventRepository.save(outboxEvent);
    }

    public void markFailed(OutboxEvent outboxEvent, Exception exception) {
        outboxEvent.setStatus(OutboxEventStatus.FAILED);
        outboxEvent.setRetryCount(outboxEvent.getRetryCount() + 1);
        outboxEvent.setErrorMessage(exception.getMessage());
        outboxEventRepository.save(outboxEvent);
    }
}
