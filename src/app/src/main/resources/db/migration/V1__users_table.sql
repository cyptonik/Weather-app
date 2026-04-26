CREATE TABLE Users (
    id serial not null primary key,
    login varchar(72) not null unique,
    password varchar(72) not null
);