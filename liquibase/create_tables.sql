DROP TABLE IF EXISTS mydb.sample_entity;
DROP SCHEMA IF EXISTS mydb;

CREATE SCHEMA IF NOT EXISTS mydb;
CREATE TABLE IF NOT EXISTS mydb.sample_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    quantity INTEGER NOT NULL,
    stock BIGINT,
    weight DOUBLE PRECISION,
    ratio REAL,
    price NUMERIC(15,2),
    active BOOLEAN NOT NULL,
    category CHAR(1),
    manufactured_date DATE,
    manufactured_time TIME,
    created_at TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    external_id UUID UNIQUE,
    document BYTEA,
    comments TEXT,
    status VARCHAR(20),
    version BIGINT
);