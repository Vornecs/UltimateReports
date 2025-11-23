# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;

# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

# Create the users table if it does not exist
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`        char(36)      NOT NULL UNIQUE PRIMARY KEY,
    `username`    varchar(16)   NOT NULL,
    `preferences` longblob      NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;



# Create the reports table if it does not exist
CREATE TABLE IF NOT EXISTS `%reports_data%`
(
    `id`                int                  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `data`              longblob             NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;