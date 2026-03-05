-- Dev seed: Google MCC connection for the demo tenant
INSERT INTO google_connections (
    id, tenant_id, manager_id, status, credential_path,
    last_discovered_at, created_at, updated_at
) VALUES (
    '30000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '858-270-7576',
    'ACTIVE',
    'infra/secrets/google-search-creds.json',
    NOW(),
    NOW(),
    NOW()
);

-- Dev seed: account graph state for the test account
INSERT INTO account_graph_state (
    id, connection_id, customer_id, account_name, currency_code, time_zone, discovered_at
) VALUES (
    '30000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000001',
    '506-204-8043',
    'Test Account',
    'USD',
    'America/New_York',
    NOW()
);

-- Dev seed: active account binding for the test account
INSERT INTO account_bindings (
    id, connection_id, tenant_id, customer_id, status, binding_type,
    created_at, updated_at
) VALUES (
    '30000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '506-204-8043',
    'ACTIVE',
    'OWNED',
    NOW(),
    NOW()
);
