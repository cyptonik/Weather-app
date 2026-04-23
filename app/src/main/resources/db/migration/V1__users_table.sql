CREATE TABLE Users (
    id serial not null primary key,
    login varchar(50) not null unique,
    password varchar(50) not null
);