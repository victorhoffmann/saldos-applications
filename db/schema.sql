CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    owner UUID NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    balance_amount NUMERIC(15,2) NOT NULL,
    balance_currency VARCHAR(3) NOT NULL,
    last_transaction_timestamp BIGINT
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    type VARCHAR(10) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(12) NOT NULL,
    timestamp_original BIGINT NOT NULL,              
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(account_id, id)                     
);

CREATE INDEX IF NOT EXISTS idx_tx_account_ts
ON transactions(account_id, ts_original);
