ALTER TABLE account_bindings
    ADD COLUMN org_node_id UUID,
    ADD COLUMN org_scope_type VARCHAR(30);
