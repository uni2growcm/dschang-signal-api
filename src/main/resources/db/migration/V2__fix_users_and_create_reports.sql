-- Add updated_at column to reports
ALTER TABLE reports ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- Fix description column from VARCHAR to TEXT
ALTER TABLE reports ALTER COLUMN description TYPE TEXT;

-- Fix rejection_reason column from VARCHAR to TEXT
ALTER TABLE reports ALTER COLUMN rejection_reason TYPE TEXT;