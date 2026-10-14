-- Feedback Management System - MySQL schema
-- The application can create these tables automatically (spring.jpa.hibernate.ddl-auto=update),
-- but this script documents the schema and can be applied manually.

CREATE DATABASE IF NOT EXISTS feedback_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE feedback_db;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(120) NOT NULL,
    password   VARCHAR(100) NOT NULL,           -- BCrypt hash, never plaintext
    role       VARCHAR(20)  NOT NULL,           -- USER or ADMIN
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS feedback (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    user_id    BIGINT        NOT NULL,
    message    VARCHAR(2000) NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    updated_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    -- Enforces the business rule: exactly one feedback entry per user.
    CONSTRAINT uk_feedback_user_id UNIQUE (user_id),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_feedback_created_at (created_at)
) ENGINE = InnoDB;
