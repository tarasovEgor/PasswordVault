CREATE TABLE password_entries (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL CHECK (length(trim(name)) > 0),
    encrypted_password TEXT NOT NULL CHECK (length(encrypted_password) > 0),
    comment TEXT NULL,
    created BIGINT NOT NULL,
    deleted BIGINT NULL
);

CREATE INDEX idx_password_entries_not_deleted
    ON password_entries (id)
    WHERE deleted IS NULL;