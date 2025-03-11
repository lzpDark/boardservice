CREATE TABLE users (
    username TEXT COLLATE NOCASE NOT NULL PRIMARY KEY,
    password TEXT NOT NULL,
    enabled BOOLEAN NOT NULL
);
CREATE TABLE anonymous_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,
    createdTime DATETIME
);

CREATE TABLE authorities (
    username TEXT COLLATE NOCASE NOT NULL,
    authority TEXT COLLATE NOCASE NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

--  remember-me persistent
CREATE TABLE persistent_logins (
    username  VARCHAR(64) NOT NULL,
    series    VARCHAR(64) PRIMARY KEY,
    token     VARCHAR(64) NOT NULL,
    last_used TIMESTAMP   NOT NULL
);
