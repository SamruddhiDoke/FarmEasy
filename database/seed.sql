-- Seed data for FARM EASY (run after schema or let application create tables)
USE frameasy;

-- Roles already inserted by schema.sql: ROLE_FARMER, ROLE_CUSTOMER, ROLE_ADMIN

-- Admin user (password: password)
INSERT INTO users (name, email, phone, password, location, preferred_language, is_verified, is_active, created_at, updated_at)
VALUES ('Admin', 'admin@frameasy.com', '9999999999', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'India', 'en', 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;
SET @admin_id = (SELECT id FROM users WHERE email = 'admin@frameasy.com' LIMIT 1);
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT @admin_id, id FROM roles WHERE name = 'ROLE_ADMIN';

-- Sample farmer (password: password)
INSERT INTO users (name, email, phone, password, location, latitude, longitude, preferred_language, farm_size, is_verified, is_active, created_at, updated_at)
VALUES ('Ramesh Kumar', 'farmer1@test.com', '9876543210', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pune, Maharashtra', 18.5204, 73.8567, 'en', '5 acres', 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;
SET @f1 = (SELECT id FROM users WHERE email = 'farmer1@test.com' LIMIT 1);
INSERT IGNORE INTO user_roles (user_id, role_id) SELECT @f1, id FROM roles WHERE name = 'ROLE_FARMER';

-- Sample equipment (if farmer exists)
INSERT INTO equipment (user_id, title, description, price_per_day, category, availability, location, latitude, longitude, is_approved, is_active, created_at, updated_at)
SELECT @f1, 'Tractor John Deere', 'Good condition tractor for rent', 1500.00, 'Tractor', 'Available', 'Pune', 18.5204, 73.8567, 1, 1, NOW(), NOW()
FROM users WHERE id = @f1 LIMIT 1;

INSERT INTO equipment (user_id, title, description, price_per_day, category, availability, location, latitude, longitude, is_approved, is_active, created_at, updated_at)
SELECT @f1, 'Harvester', 'Combine harvester for wheat', 5000.00, 'Harvester', 'Seasonal', 'Pune', 18.52, 73.85, 1, 1, NOW(), NOW()
FROM users WHERE id = @f1 LIMIT 1;

-- Sample trade (crops) - approved
INSERT INTO trade (user_id, crop_name, description, price_per_unit, unit, quantity, location, latitude, longitude, is_approved, is_active, created_at, updated_at)
SELECT @f1, 'Wheat', 'Organic wheat, freshly harvested', 25.00, 'kg', '500 kg', 'Pune, Maharashtra', 18.5204, 73.8567, 1, 1, NOW(), NOW()
FROM users WHERE id = @f1 LIMIT 1;

INSERT INTO trade (user_id, crop_name, description, price_per_unit, unit, quantity, location, latitude, longitude, is_approved, is_active, created_at, updated_at)
SELECT @f1, 'Rice', 'Basmati rice premium quality', 45.00, 'kg', '200 kg', 'Pune', 18.52, 73.85, 1, 1, NOW(), NOW()
FROM users WHERE id = @f1 LIMIT 1;

update users set password='$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' where email='admin@frameasy.com';