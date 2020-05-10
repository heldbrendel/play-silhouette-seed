# --- !Ups

create table users
(
    user_id    serial,
    username   text not null,
    email      text not null,
    first_name text,
    last_name  text,
    activated  bool,
    created    timestamp,
    modified   timestamp
);

create table auth_token
(
    auth_token_id uuid not null,
    user_id       int  not null,
    expiry        timestamp
);

create table password_info
(
    user_name text not null,
    hasher    text not null,
    password  text not null,
    salt      text
);

# --- !Downs

drop table if exists password_info;
drop table if exists auth_token;
drop table if exists users;
