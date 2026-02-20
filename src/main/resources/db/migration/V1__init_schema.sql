CREATE TABLE accounts (
    id             BIGSERIAL    PRIMARY KEY,
    service_type   VARCHAR(30)  NOT NULL,
    account_number VARCHAR(30)  NOT NULL,
    CONSTRAINT uq_account_service_number UNIQUE (service_type, account_number)
);

CREATE TABLE payments (
    id           BIGSERIAL      PRIMARY KEY,
    account_id   BIGINT         NOT NULL REFERENCES accounts(id),
    amount       NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    payment_date DATE           NOT NULL
);

CREATE INDEX idx_payments_account_id ON payments(account_id);

CREATE TABLE meter_readings (
    id           BIGSERIAL   PRIMARY KEY,
    account_id   BIGINT      NOT NULL REFERENCES accounts(id),
    value        VARCHAR(10) NOT NULL,
    reading_date DATE        NOT NULL
);

CREATE INDEX idx_meter_readings_account_id ON meter_readings(account_id);
