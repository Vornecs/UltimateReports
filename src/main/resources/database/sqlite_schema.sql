-- Create the users table if it does not exist
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`          TEXT          NOT NULL PRIMARY KEY,
    `username`      TEXT          NOT NULL,
    `preferences`   BLOB          NOT NULL
);



-- Create the reports table if it does not exist
CREATE TABLE IF NOT EXISTS `%reports_data%`
(
    `id`                INTEGER           PRIMARY KEY AUTOINCREMENT,
    `data`              BLOB              NOT NULL
);