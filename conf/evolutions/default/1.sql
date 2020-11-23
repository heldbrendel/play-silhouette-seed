-- !Ups

CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    email    VARCHAR
);

CREATE TABLE password_infos
(
    username VARCHAR NOT NULL,
    hasher   VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    salt     VARCHAR

);

-- !Downs

DROP TABLE password_infos;
DROP TABLE users;
