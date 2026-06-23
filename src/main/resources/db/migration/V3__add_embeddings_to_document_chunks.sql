ALTER TABLE document_chunks
ADD COLUMN embedding JSONB,
ADD COLUMN embedding_model VARCHAR(100),
ADD COLUMN embedded_at TIMESTAMP;