CREATE TABLE Sessions(
    id uuid not null,
    user_id     INTEGER NOT NULL REFERENCES Users(id),
    expires_at timestamp not null
);