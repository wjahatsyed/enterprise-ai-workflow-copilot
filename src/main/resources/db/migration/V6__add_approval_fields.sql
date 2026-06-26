ALTER TABLE workflow_runs
ADD COLUMN approval_status VARCHAR(30),
ADD COLUMN approved_by VARCHAR(255),
ADD COLUMN approved_at TIMESTAMP,
ADD COLUMN rejection_reason TEXT;