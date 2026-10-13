CREATE TABLE departments (
    code VARCHAR(20)  PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    id              BIGSERIAL    PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(200) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    department_code VARCHAR(20)  NOT NULL REFERENCES departments (code)
);

CREATE TABLE cost_centres (
    code            VARCHAR(30)  PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    department_code VARCHAR(20)  NOT NULL REFERENCES departments (code)
);

CREATE TABLE claims (
    id               BIGSERIAL     PRIMARY KEY,
    owner_id         BIGINT        NOT NULL REFERENCES users (id),
    owner_username   VARCHAR(100)  NOT NULL,
    title            VARCHAR(200)  NOT NULL,
    amount           NUMERIC(12,2) NOT NULL,
    expense_date     DATE          NOT NULL,
    notes            TEXT,
    status           VARCHAR(20)   NOT NULL,
    approver_id      BIGINT        REFERENCES users (id),
    receipt_name     VARCHAR(255),
    receipt_checksum VARCHAR(64),
    receipt_data     BYTEA,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

ALTER SEQUENCE claims_id_seq RESTART WITH 1001;

CREATE INDEX idx_claims_owner ON claims (owner_id);
CREATE INDEX idx_claims_approver_status ON claims (approver_id, status);
CREATE INDEX idx_claims_receipt_checksum ON claims (receipt_checksum);

CREATE TABLE claim_allocations (
    id               BIGSERIAL     PRIMARY KEY,
    claim_id         BIGINT        NOT NULL REFERENCES claims (id) ON DELETE CASCADE,
    cost_centre_code VARCHAR(30)   NOT NULL REFERENCES cost_centres (code),
    amount           NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_claim_allocations_claim ON claim_allocations (claim_id);

CREATE TABLE claim_events (
    id             BIGSERIAL    PRIMARY KEY,
    claim_id       BIGINT       NOT NULL REFERENCES claims (id) ON DELETE CASCADE,
    action         VARCHAR(20)  NOT NULL,
    actor_username VARCHAR(100) NOT NULL,
    comment        TEXT,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_claim_events_claim ON claim_events (claim_id);
