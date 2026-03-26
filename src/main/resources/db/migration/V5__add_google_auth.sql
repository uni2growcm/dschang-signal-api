
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);

UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;

COMMENT ON COLUMN users.google_id IS 'Google user ID for OAuth';
COMMENT ON COLUMN users.avatar_url IS 'Avatar URL from Google';
COMMENT ON COLUMN users.auth_provider IS 'Authentication provider (LOCAL or GOOGLE)';