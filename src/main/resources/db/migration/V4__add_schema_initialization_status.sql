ALTER TABLE schema_migrations
ADD COLUMN schema_initialization_status VARCHAR(30)
NOT NULL DEFAULT 'NOT_STARTED';