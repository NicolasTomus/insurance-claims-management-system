-- RISK FACTOR CONFIGURATIONS
-- level: COUNTRY/COUNTY/CITY/BUILDING_TYPE
-- reference_id points to the corresponding entity id (except BUILDING_TYPE which is free-form in your schema)
-- --------------------
INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 1, true, 3.00, 'COUNTRY', 1
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 1);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 2, true, 2.00, 'COUNTRY', 2
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 2);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 3, true, 1.50, 'COUNTRY', 3
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 3);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 4, true, 5.00, 'COUNTY', 1
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 4);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 5, true, 4.00, 'COUNTY', 2
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 5);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 6, true, 7.00, 'CITY', 1
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 6);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 7, true, 6.00, 'CITY', 3
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 7);

-- BUILDING_TYPE examples (no FK enforced; just some reference ids you can interpret in code)
INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 8, true, 8.00, 'BUILDING_TYPE', 1
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 8);

INSERT INTO risk_factor_configurations (id, active, adjustment_percentage, level, reference_id)
SELECT 9, true, 12.00, 'BUILDING_TYPE', 2
    WHERE NOT EXISTS (SELECT 1 FROM risk_factor_configurations WHERE id = 9);

-- --------------------
