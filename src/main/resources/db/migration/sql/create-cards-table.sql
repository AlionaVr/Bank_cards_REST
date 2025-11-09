CREATE TABLE IF NOT EXISTS cards
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    card_number      VARCHAR(16)    NOT NULL UNIQUE,
    card_holder_name VARCHAR(255)   NOT NULL,
    balance          NUMERIC(19, 2) NOT NULL DEFAULT 0,
    user_id          UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_date     DATE           NOT NULL DEFAULT CURRENT_DATE,
    expiry_date      DATE           NOT NULL,
    status           VARCHAR(50)    NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX IF NOT EXISTS idx_card_owner ON cards (user_id);
CREATE INDEX IF NOT EXISTS idx_card_status ON cards (status);