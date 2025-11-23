-- Users table
CREATE TABLE IF NOT EXISTS %user_data% (
    uuid         CHAR(36)     NOT NULL PRIMARY KEY,
    username     VARCHAR(16)  NOT NULL,
    preferences  VARCHAR         NOT NULL
    );

-- Teams table
CREATE TABLE IF NOT EXISTS %reports_data% (
    id    INT          PRIMARY KEY AUTO_INCREMENT,
    data  VARCHAR         NOT NULL
    );