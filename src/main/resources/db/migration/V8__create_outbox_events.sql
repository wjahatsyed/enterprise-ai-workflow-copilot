CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    payload_json JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);

CREATE INDEX idx_outbox_events_status_created_at
ON outbox_events(status, created_at);
