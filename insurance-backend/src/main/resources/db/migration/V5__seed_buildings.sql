-- BUILDINGS (building_type: RESIDENTIAL/OFFICE/INDUSTRIAL)
-- FK: city_id -> cities, owner_id -> clients
-- --------------------
INSERT INTO buildings (
    id, address, building_type, construction_year,
    earthquake_risk_zone, flood_zone,
    insured_value, number_of_floors, surface_area,
    city_id, owner_id
)
SELECT
    1, 'Str. Universitatii 5, Timisoara', 'RESIDENTIAL', 2008,
    true, false,
    250000.00, 6, 900.50,
    1, 1
    WHERE NOT EXISTS (SELECT 1 FROM buildings WHERE id = 1);

INSERT INTO buildings (
    id, address, building_type, construction_year,
    earthquake_risk_zone, flood_zone,
    insured_value, number_of_floors, surface_area,
    city_id, owner_id
)
SELECT
    2, 'Calea Motilor 20, Timisoara', 'OFFICE', 2016,
    false, false,
    1250000.00, 10, 3200.00,
    1, 2
    WHERE NOT EXISTS (SELECT 1 FROM buildings WHERE id = 2);

INSERT INTO buildings (
    id, address, building_type, construction_year,
    earthquake_risk_zone, flood_zone,
    insured_value, number_of_floors, surface_area,
    city_id, owner_id
)
SELECT
    3, 'Str. Decebal 3, Oradea', 'RESIDENTIAL', 1999,
    true, true,
    180000.00, 4, 650.00,
    2, 3
    WHERE NOT EXISTS (SELECT 1 FROM buildings WHERE id = 3);

INSERT INTO buildings (
    id, address, building_type, construction_year,
    earthquake_risk_zone, flood_zone,
    insured_value, number_of_floors, surface_area,
    city_id, owner_id
)
SELECT
    4, 'Industrial Park 8, Budapest', 'INDUSTRIAL', 2012,
    false, true,
    3500000.00, 2, 12000.00,
    3, 4
    WHERE NOT EXISTS (SELECT 1 FROM buildings WHERE id = 4);

INSERT INTO buildings (
    id, address, building_type, construction_year,
    earthquake_risk_zone, flood_zone,
    insured_value, number_of_floors, surface_area,
    city_id, owner_id
)
SELECT
    5, 'Leopoldstrasse 100, Munich', 'OFFICE', 2005,
    false, false,
    2200000.00, 8, 5400.00,
    4, 5
    WHERE NOT EXISTS (SELECT 1 FROM buildings WHERE id = 5);

-- --------------------
