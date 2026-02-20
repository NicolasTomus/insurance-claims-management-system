-- COUNTRIES
-- --------------------
INSERT INTO countries (id, name)
SELECT 1, 'Romania'
    WHERE NOT EXISTS (SELECT 1 FROM countries WHERE id = 1);

INSERT INTO countries (id, name)
SELECT 2, 'Hungary'
    WHERE NOT EXISTS (SELECT 1 FROM countries WHERE id = 2);

INSERT INTO countries (id, name)
SELECT 3, 'Germany'
    WHERE NOT EXISTS (SELECT 1 FROM countries WHERE id = 3);

-- --------------------


-- COUNTIES (FK -> countries)
-- --------------------
INSERT INTO counties (id, name, country_id)
SELECT 1, 'Timis', 1
    WHERE NOT EXISTS (SELECT 1 FROM counties WHERE id = 1);

INSERT INTO counties (id, name, country_id)
SELECT 2, 'Bihor', 1
    WHERE NOT EXISTS (SELECT 1 FROM counties WHERE id = 2);

INSERT INTO counties (id, name, country_id)
SELECT 3, 'Budapest', 2
    WHERE NOT EXISTS (SELECT 1 FROM counties WHERE id = 3);

INSERT INTO counties (id, name, country_id)
SELECT 4, 'Bavaria', 3
    WHERE NOT EXISTS (SELECT 1 FROM counties WHERE id = 4);

-- --------------------


-- CITIES (FK -> counties)
-- --------------------
INSERT INTO cities (id, name, county_id)
SELECT 1, 'Timisoara', 1
    WHERE NOT EXISTS (SELECT 1 FROM cities WHERE id = 1);

INSERT INTO cities (id, name, county_id)
SELECT 2, 'Oradea', 2
    WHERE NOT EXISTS (SELECT 1 FROM cities WHERE id = 2);

INSERT INTO cities (id, name, county_id)
SELECT 3, 'Budapest', 3
    WHERE NOT EXISTS (SELECT 1 FROM cities WHERE id = 3);

INSERT INTO cities (id, name, county_id)
SELECT 4, 'Munich', 4
    WHERE NOT EXISTS (SELECT 1 FROM cities WHERE id = 4);

-- --------------------


-- CURRENCIES (no UNIQUE on code -> use id checks)
-- --------------------
INSERT INTO currencies (id, active, code, exchange_rate_to_base, name)
SELECT 1, true, 'EUR', 1.00000000, 'Euro'
    WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE id = 1);

INSERT INTO currencies (id, active, code, exchange_rate_to_base, name)
SELECT 2, true, 'RON', 0.20000000, 'Romanian Leu'
    WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE id = 2);

INSERT INTO currencies (id, active, code, exchange_rate_to_base, name)
SELECT 3, true, 'HUF', 0.00260000, 'Hungarian Forint'
    WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE id = 3);

-- --------------------
