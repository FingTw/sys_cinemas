-- Create Main Database
-- This might fail if we are already connected to cinema_db, 
-- but in Docker postgres image, POSTGRES_DB is created first.
-- So we use this script to create schemas.

-- Create Databases for Microservices
CREATE DATABASE auth_db;
CREATE DATABASE management_db;
CREATE DATABASE booking_db;

-- Connect to each and create schemas if needed (Optional, usually we just use public schema when separated)
