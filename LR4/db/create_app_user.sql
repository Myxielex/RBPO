-- Выполнять в MySQL Workbench под администратором MySQL.
-- Перед запуском замените REPLACE_WITH_STRONG_PASSWORD на свой пароль.

CREATE DATABASE IF NOT EXISTS recycling
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'recycling_app'@'localhost'
    IDENTIFIED BY 'REPLACE_WITH_STRONG_PASSWORD';

-- Права ограничены только базой recycling, а не всем сервером MySQL.
-- CREATE/ALTER/INDEX/REFERENCES нужны Hibernate при ddl-auto: update.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON recycling.* TO 'recycling_app'@'localhost';

FLUSH PRIVILEGES;
