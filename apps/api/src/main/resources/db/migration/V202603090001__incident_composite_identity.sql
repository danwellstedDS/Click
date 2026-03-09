ALTER TABLE execution_incidents
    ADD COLUMN revision_id UUID,
    ADD COLUMN item_id UUID,
    ADD COLUMN failure_class_key VARCHAR(30);

CREATE INDEX idx_incidents_composite
    ON execution_incidents (revision_id, item_id, failure_class_key);
