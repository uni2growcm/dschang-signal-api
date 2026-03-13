-- Insert mock users
INSERT INTO users (email, password, full_name, role, created_at, updated_at, is_active)
VALUES
    ('jean.paul@u2g.com', '$2a$10$X7VYx/h1h9Yx7Yx7Yx7YxO9Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Y', 'Jean Paul', 'CITIZEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('jeanne.marie@u2g.com', '$2a$10$X7VYx/h1h9Yx7Yx7Yx7YxO9Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Y', 'Jeanne Marie', 'CITIZEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('admin@dschang-signal.com', '$2a$10$X7VYx/h1h9Yx7Yx7Yx7YxO9Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Yx7Y', 'Admin User', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Insert mock categories
INSERT INTO categories (name, color, created_at, created_by)
VALUES
    ('Road Damage', '#FF0000', CURRENT_TIMESTAMP, 3),
    ('Street Lighting', '#FFFF00', CURRENT_TIMESTAMP, 3),
    ('Waste Management', '#00FF00', CURRENT_TIMESTAMP, 3),
    ('Public Safety', '#0000FF', CURRENT_TIMESTAMP, 3),
    ('Water Supply', '#00FFFF', CURRENT_TIMESTAMP, 3);

-- Insert mock reports (with created_by)
INSERT INTO reports (title, description, location_text, moderation_status, report_status, rejection_reason, created_at, reviewed_at, updated_at, created_by)
VALUES
    ('Pothole on Main Street', 'Large pothole causing damage to vehicles', 'Main Street & 5th Avenue', 'RESOLVED', 'CLOSED', NULL, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '25 days', 1),
    ('Street light not working', 'Street light has been out for 2 weeks', 'Oak Avenue near Central Park','RESOLVED', 'IN_PROGRESS', NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 2),
    ('Illegal dumping', 'Someone dumped construction waste', '123 Industrial Road', 'PENDING', 'REPORTED', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', 1);

-- Insert mock medias
INSERT INTO medias (description, type, url, original_name, mime_type, file_size, created_at, updated_at, created_by, report_id)
VALUES
    ('Close-up of pothole', 'IMAGE', 'https://example.com/photos/pothole1_closeup.jpg', 'pothole_closeup.jpg', 'image/jpeg', 2048576, CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, 1, 1),
    ('Wide angle of street', 'IMAGE', 'https://example.com/photos/streetlight1_wide.jpg', 'street_wide.jpg', 'image/jpeg', 3145728, CURRENT_TIMESTAMP - INTERVAL '15 days', NULL, 2, 2),
    ('Video of illegal dumping', 'VIDEO', 'https://example.com/videos/dumping1.mp4', 'dumping_video.mp4', 'video/mp4', 15728640, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, 1, 3);

-- Link reports to categories
INSERT INTO report_category (report_id, category_id)
VALUES
    (1, 1), -- Road Damage
    (2, 2), -- Street Lighting
    (3, 3); -- Waste Management