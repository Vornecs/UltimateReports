-- Create users table
CREATE TABLE IF NOT EXISTS %user_data% (
    uuid         CHAR(36)     PRIMARY KEY,
    username     VARCHAR(16)  NOT NULL,
    preferences  TEXT        NOT NULL
    );

-- Create teams table
CREATE TABLE IF NOT EXISTS %reports_data% (
    id    SERIAL        PRIMARY KEY,
    data  TEXT         NOT NULL
    );