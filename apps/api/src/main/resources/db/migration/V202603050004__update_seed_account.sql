-- Update dev seed account to 506-204-8043
UPDATE account_graph_state
SET customer_id = '506-204-8043', account_name = 'Test Account'
WHERE customer_id = '228-236-3670';

UPDATE account_bindings
SET customer_id = '506-204-8043'
WHERE customer_id = '228-236-3670';
