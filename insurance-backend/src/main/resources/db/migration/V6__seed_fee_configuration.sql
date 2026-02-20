-- FEE CONFIGURATION (type: BROKER_COMMISSION / RISK_ADJUSTMENT / ADMIN_FEE)
-- include multiple periods to test date filtering
-- --------------------
INSERT INTO fee_configuration (id, active, effective_from, effective_to, name, percentage, type)
SELECT 1, true, DATE '2025-01-01', NULL, 'Admin fee (default)', 2.50, 'ADMIN_FEE'
    WHERE NOT EXISTS (SELECT 1 FROM fee_configuration WHERE id = 1);

INSERT INTO fee_configuration (id, active, effective_from, effective_to, name, percentage, type)
SELECT 2, true, DATE '2025-01-01', NULL, 'Broker commission (default)', 5.00, 'BROKER_COMMISSION'
    WHERE NOT EXISTS (SELECT 1 FROM fee_configuration WHERE id = 2);

INSERT INTO fee_configuration (id, active, effective_from, effective_to, name, percentage, type)
SELECT 3, true, DATE '2025-01-01', NULL, 'Risk adjustment (default)', 10.00, 'RISK_ADJUSTMENT'
    WHERE NOT EXISTS (SELECT 1 FROM fee_configuration WHERE id = 3);

-- A "historical" config (ended)
INSERT INTO fee_configuration (id, active, effective_from, effective_to, name, percentage, type)
SELECT 4, false, DATE '2024-01-01', DATE '2024-12-31', 'Admin fee (2024)', 2.00, 'ADMIN_FEE'
    WHERE NOT EXISTS (SELECT 1 FROM fee_configuration WHERE id = 4);

-- --------------------
