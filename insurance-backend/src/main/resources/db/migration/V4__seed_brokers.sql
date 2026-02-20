-- BROKERS (status: 0/1)
-- --------------------
INSERT INTO broker (id, broker_code, commission_percentage, email, name, phone, status)
SELECT 1, 'BRK-001', 5.00, 'broker1@example.com', 'Broker One', '+40-700-555-666', 1
    WHERE NOT EXISTS (SELECT 1 FROM broker WHERE id = 1);

INSERT INTO broker (id, broker_code, commission_percentage, email, name, phone, status)
SELECT 2, 'BRK-002', 3.50, 'broker2@example.com', 'Broker Two', '+40-700-777-888', 1
    WHERE NOT EXISTS (SELECT 1 FROM broker WHERE id = 2);

INSERT INTO broker (id, broker_code, commission_percentage, email, name, phone, status)
SELECT 3, 'BRK-003', 4.25, 'broker3@example.com', 'Broker Three', '+49-151-999-0000', 0
    WHERE NOT EXISTS (SELECT 1 FROM broker WHERE id = 3);

-- --------------------
