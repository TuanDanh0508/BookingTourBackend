CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS m_client (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL, -- HOTEL | BUS | RESTAURANT | ATTRACTION | GUIDE | TRANSPORTATION
    contact_name VARCHAR(255),
    phone        VARCHAR(50),
    email        VARCHAR(255),
    address      TEXT,
    description  TEXT,
    is_active    BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_client_name_type UNIQUE (name, type)
);
