CREATE TABLE IF NOT EXISTS transfers
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    from_card_id  UUID           NOT NULL,
    to_card_id    UUID           NOT NULL,

    amount        NUMERIC(18, 2) NOT NULL,
    description   VARCHAR(500),

    transfer_date TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transfer_from_card FOREIGN KEY (from_card_id)
        REFERENCES cards (id) ON DELETE CASCADE,

    CONSTRAINT fk_transfer_to_card FOREIGN KEY (to_card_id)
        REFERENCES cards (id) ON DELETE CASCADE
);

CREATE INDEX idx_transfer_from_card ON transfers (from_card_id);
CREATE INDEX idx_transfer_to_card ON transfers (to_card_id);