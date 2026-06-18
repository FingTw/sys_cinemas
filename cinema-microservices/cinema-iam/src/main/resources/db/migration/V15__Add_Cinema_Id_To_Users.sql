-- V15__Add_Cinema_Id_To_Users.sql
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS cinema_id VARCHAR(255);
