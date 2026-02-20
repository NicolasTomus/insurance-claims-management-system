-- POLICIES
-- status: DRAFT/ACTIVE/EXPIRED/CANCELLED
-- multiple currencies, brokers, buildings, clients
-- --------------------
INSERT INTO policies (
    id,
    base_premium_amount, end_date, final_premium_amount,
    policy_number, start_date, status,
    broker_id, building_id, client_id, currency_id,
    created_at, updated_at
)
SELECT
    1,
    1000.00, DATE '2026-01-01', 1125.00,
    'POL-2025-0001', DATE '2025-01-01', 'ACTIVE',
    1, 1, 1, 1,now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM policies WHERE id = 1);

INSERT INTO policies (
    id,
    base_premium_amount, end_date, final_premium_amount,
    policy_number, start_date, status,
    broker_id, building_id, client_id, currency_id,
    created_at, updated_at
)
SELECT
    2,
    2500.00, DATE '2027-02-01', 2875.00,
    'POL-2026-0002', DATE '2026-02-01', 'DRAFT',
    2, 2, 2, 1, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM policies WHERE id = 2);

INSERT INTO policies (
    id,
    base_premium_amount, end_date, final_premium_amount,
    policy_number, start_date, status,
    broker_id, building_id, client_id, currency_id,
    created_at, updated_at
)
SELECT
    3,
    800.00, DATE '2025-12-31', 920.00,
    'POL-2025-0003', DATE '2025-01-01', 'EXPIRED',
    1, 3, 3, 2, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM policies WHERE id = 3);

INSERT INTO policies (
    id,
    base_premium_amount, end_date, final_premium_amount,
    policy_number, start_date, status,
    broker_id, building_id, client_id, currency_id,
    created_at, updated_at
)
SELECT
    4,
    7000.00, DATE '2026-06-30', 8500.00,
    'POL-2026-0004', DATE '2026-01-01', 'CANCELLED',
    2, 4, 4, 3, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM policies WHERE id = 4);

INSERT INTO policies (
    id,
    base_premium_amount, end_date, final_premium_amount,
    policy_number, start_date, status,
    broker_id, building_id, client_id, currency_id,
    created_at, updated_at
)
SELECT
    5,
    4200.00, DATE '2026-12-31', 4700.00,
    'POL-2026-0005', DATE '2026-01-15', 'ACTIVE',
    3, 5, 5, 1, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM policies WHERE id = 5);
