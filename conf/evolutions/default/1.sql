-- Play schema

-- !Ups

create table USER
(
  userID    uuid    NOT NULL PRIMARY KEY,
  firstName varchar(255),
  lastName  varchar(255),
  fullName  varchar(255),
  email     varchar(255),
  avatarURL varchar(255),
  activated boolean NOT NULL
);

create table LOGININFO
(
  id          bigint(20)   NOT NULL AUTO_INCREMENT,
  providerID  varchar(255) NOT NULL,
  providerKey varchar(255) NOT NULL
);

create table USERLOGININFO
(
  userID      uuid       NOT NULL,
  loginInfoId bigint(20) NOT NULL
);

create table PASSWORDINFO
(
  hasher      varchar(255) NOT NULL,
  password    varchar(255) NOT NULL,
  salt        varchar(255),
  loginInfoId bigint(20)   NOT NULL
);

create table AUTHTOKEN
(
  id     uuid      NOT NULL PRIMARY KEY,
  userID uuid      NOT NULL,
  expiry timestamp NOT NULL
);

-- !Downs

drop table AUTHTOKEN;
drop table PASSWORDINFO;
drop table USERLOGININFO;
drop table LOGININFO;
drop table USER;
