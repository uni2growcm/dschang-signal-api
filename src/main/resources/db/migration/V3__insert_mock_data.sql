-- Insert mock users
INSERT INTO users (email, password, full_name, role, created_at, updated_at, is_active)
VALUES
    ('jean.paul@u2g.com', '$2y$12$s2YhTUNF42QblCbYnzPaBOYxPkT98.ZJAcw6uO3ghxTlng7YBGxey', 'Jean Paul', 'CITIZEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('jeanne.marie@u2g.com', '$2y$12$s2YhTUNF42QblCbYnzPaBOYxPkT98.ZJAcw6uO3ghxTlng7YBGxey', 'Jeanne Marie', 'CITIZEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('admin@dschang-signal.com', '$2y$12$s2YhTUNF42QblCbYnzPaBOYxPkT98.ZJAcw6uO3ghxTlng7YBGxey', 'Admin User', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Insert mock categories
INSERT INTO categories (name, color, created_at, created_by)
VALUES
    ('Roads & Potholes', '#FF0000', CURRENT_TIMESTAMP, 1),
    ('Street Lighting', '#FFFF00', CURRENT_TIMESTAMP, 2),
    ('Waste Management', '#00FF00', CURRENT_TIMESTAMP, 3),
    ('Public Safety', '#0000FF', CURRENT_TIMESTAMP, 2),
    ('Water Supply', '#00FFFF', CURRENT_TIMESTAMP, 3),
    ('Electricity', '#FF00FF', CURRENT_TIMESTAMP, 1),
    ('Public Transport', '#800080', CURRENT_TIMESTAMP, 1),
    ('Markets & Stalls', '#FFA500', CURRENT_TIMESTAMP, 2),
    ('Drainage & Canals', '#008000', CURRENT_TIMESTAMP, 1),
    ('Noise Pollution', '#800000', CURRENT_TIMESTAMP, 3);

-- Insert mock reports (with created_by)
INSERT INTO reports (title, description, location_text, moderation_status, report_status, rejection_reason, created_at, reviewed_at, updated_at, created_by)
VALUES
    ('Dangerous pothole at Market Junction','A huge pothole has formed at the main market junction. Several motorcycles have already fallen. Urgent repair needed.', 'Market Junction, Dschang','ACCEPTED', 'RESOLVED', NULL, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '25 days', 1),
    ('Broken streetlights in Foto neighborhood', 'All streetlights in the Foto neighborhood have been broken for 3 weeks. At night, the area is completely dark, encouraging insecurity.', 'Foto neighborhood, behind the University', 'ACCEPTED', 'IN_PROGRESS', NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 2),
    ('Illegal dumping behind the Chief''s Palace', 'Trucks come at night to dump waste behind the Chief''s Palace. The smell is unbearable and attracts flies.', 'Behind the Chief''s Palace, Dschang', 'PENDING_REVIEW', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', 1),
    ('Clogged drainage canal in Foreke neighborhood', 'The water drainage canal is completely blocked. With the upcoming rains, houses risk being flooded.', 'Foreke neighborhood, near the public school', 'ACCEPTED', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '7 days', NULL, CURRENT_TIMESTAMP - INTERVAL '7 days',2),
    ('Frequent power outages in Toket', 'For a month, the Toket neighborhood has been experiencing power cuts every evening from 6 PM to 10 PM. Shopkeepers are losing their goods.', 'Toket neighborhood, Dschang', 'PENDING_REVIEW', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', 1);

-- Insert mock medias
INSERT INTO medias (description, type, url, original_name, mime_type, file_size, created_at, updated_at, created_by, report_id)
VALUES
    ('Photo of the pothole at the market', 'IMAGE', 'https://example.com/photos/dschang-pothole.jpg', 'market-pothole.jpg', 'image/jpeg', 2048576, CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, 1, 1),
    ('Dark streetlights in Foto', 'IMAGE', 'https://example.com/photos/foto-streetlights.jpg', 'foto-streetlights.jpg', 'image/jpeg', 3145728, CURRENT_TIMESTAMP - INTERVAL '15 days', NULL, 2, 2),
    ('Video of illegal dumping', 'VIDEO', 'https://example.com/videos/illegal-dumping.mp4', 'dumping-video.mp4', 'video/mp4', 15728640, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, 1, 3),
    ('Clogged canal in Foreke', 'IMAGE', 'https://example.com/photos/foreke-canal.jpg', 'foreke-canal.jpg', 'image/jpeg', 1835008, CURRENT_TIMESTAMP - INTERVAL '7 days', NULL, 2, 4),
    ('Power outages in Toket', 'VIDEO', 'https://example.com/videos/toket-outage.mp4', 'toket-outage.mp4', 'video/mp4', 20971520, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, 1, 5);

-- Link reports to categories
INSERT INTO report_category (report_id, category_id)
VALUES
    (1, 1), -- Roads & Potholes
    (2, 2), -- Street Lighting
    (2, 4), -- Public Safety (due to darkness)
    (3, 3), -- Waste Management
    (4, 9), -- Drainage & Canals
    (5, 6), -- Electricity
    (5, 7); -- Public Transport (impact on shopkeepers)