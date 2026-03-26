ALTER TABLE users
    ADD auth_provider VARCHAR(255);

ALTER TABLE users
    ADD avatar_url VARCHAR(255);

ALTER TABLE users
    ADD google_id VARCHAR(255);

ALTER TABLE categories
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE medias
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE medias
    ALTER COLUMN report_id SET NOT NULL;
