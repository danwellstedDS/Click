ALTER TABLE canonical_facts
    ADD COLUMN cost_amount NUMERIC(18,6) GENERATED ALWAYS AS (cost_micros / 1000000.0) STORED;
