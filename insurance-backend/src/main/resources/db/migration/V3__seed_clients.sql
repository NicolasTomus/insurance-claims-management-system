-- CLIENTS (client_type: INDIVIDUAL / COMPANY)
-- --------------------
INSERT INTO clients (id, address, client_type, email, identification_number, name, phone)
SELECT 1, 'Str. Memorandumului 1, Timisoara', 'INDIVIDUAL', 'ana.pop@example.com', 'RO123456', 'Ana Pop', '+40-700-111-222'
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE id = 1);

INSERT INTO clients (id, address, client_type, email, identification_number, name, phone)
SELECT 2, 'Bd. Eroilor 10, Timisoara', 'COMPANY', 'office@acme.ro', 'RO999888', 'ACME SRL', '+40-700-333-444'
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE id = 2);

INSERT INTO clients (id, address, client_type, email, identification_number, name, phone)
SELECT 3, 'Piata Unirii 2, Oradea', 'INDIVIDUAL', 'mihai.ionescu@example.com', 'RO222333', 'Mihai Ionescu', '+40-700-555-101'
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE id = 3);

INSERT INTO clients (id, address, client_type, email, identification_number, name, phone)
SELECT 4, 'Vaci utca 12, Budapest', 'COMPANY', 'contact@danube.hu', 'HU987654', 'Danube Consulting KFT', '+36-30-123-4567'
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE id = 4);

INSERT INTO clients (id, address, client_type, email, identification_number, name, phone)
SELECT 5, 'Marienplatz 1, Munich', 'INDIVIDUAL', 'lara.mueller@example.com', 'DE445566', 'Lara Müller', '+49-151-234-5678'
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE id = 5);

-- --------------------
