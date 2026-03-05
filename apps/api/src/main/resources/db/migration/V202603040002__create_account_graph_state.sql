CREATE TABLE account_graph_state (
    id UUID PRIMARY KEY,
    connection_id UUID NOT NULL REFERENCES google_connections(id) ON DELETE CASCADE,
    customer_id VARCHAR(20) NOT NULL,
    account_name VARCHAR(255),
    currency_code VARCHAR(10),
    time_zone VARCHAR(100),
    discovered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_graph_state_connection_customer UNIQUE (connection_id, customer_id)
);
CREATE INDEX idx_graph_state_connection_id ON account_graph_state(connection_id);
