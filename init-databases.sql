-- Create Main Database
-- This might fail if we are already connected to cinema_db, 
-- but in Docker postgres image, POSTGRES_DB is created first.
-- So we use this script to create schemas.

-- Create Schemas
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS facility;
CREATE SCHEMA IF NOT EXISTS scheduling;
CREATE SCHEMA IF NOT EXISTS booking;
CREATE SCHEMA IF NOT EXISTS keycloak;
