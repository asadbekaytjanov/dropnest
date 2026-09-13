ALTER TABLE files RENAME TO file_record;

ALTER TABLE file_record RENAME COLUMN user_id TO owner_id;
ALTER TABLE file_record RENAME COLUMN file_name TO original_name;
ALTER TABLE file_record RENAME COLUMN file_path TO storage_path;

ALTER TABLE file_record ADD COLUMN IF NOT EXISTS stored_name VARCHAR(255);
ALTER TABLE file_record ADD COLUMN IF NOT EXISTS size_bytes BIGINT;
ALTER TABLE file_record ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE file_record
SET stored_name = CONCAT(id, '_', original_name)
WHERE stored_name IS NULL;

UPDATE file_record
SET size_bytes = 0
WHERE size_bytes IS NULL;

UPDATE file_record
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

ALTER TABLE file_record ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE file_record ALTER COLUMN original_name SET NOT NULL;
ALTER TABLE file_record ALTER COLUMN storage_path SET NOT NULL;
ALTER TABLE file_record ALTER COLUMN stored_name SET NOT NULL;
ALTER TABLE file_record ALTER COLUMN size_bytes SET NOT NULL;
ALTER TABLE file_record ALTER COLUMN created_at SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_file_record_stored_name') THEN
ALTER TABLE file_record
    ADD CONSTRAINT uq_file_record_stored_name UNIQUE (stored_name);
END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_file_record_owner_id ON file_record(owner_id);
CREATE INDEX IF NOT EXISTS idx_file_record_created_at ON file_record(created_at);