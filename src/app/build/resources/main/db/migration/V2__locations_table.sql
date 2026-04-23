CREATE TABLE Locations (
    id          serial    NOT NULL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES Users(id),
    latitude    DECIMAL NOT NULL,
    longitude   DECIMAL NOT NULL
);
