-- Migration: Remove Redundant Auth Token Columns
-- Dropping columns active_token and token_version from auth.users as they are now managed in Redis

ALTER TABLE auth.users DROP COLUMN IF EXISTS active_token;
ALTER TABLE auth.users DROP COLUMN IF EXISTS token_version;
