-- Ajouter les colonnes Google à la table users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'LOCAL';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE;

-- Rendre les colonnes NOT NULL
ALTER TABLE categories
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE medias
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE medias
    ALTER COLUMN report_id SET NOT NULL;