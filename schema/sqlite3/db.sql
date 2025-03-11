CREATE TABLE task (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,
    boardId INTEGER,
    description TEXT,
    createdTime DATETIME,
    updatedTime DATETIME,
    state INTEGER DEFAULT 0, -- 0 todo, 1 InProgress, 2 done
    positionId INTEGER NOT NULL
);
CREATE TABLE board (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    username TEXT default '',
    anonymousId TEXT default '',
    description TEXT,
    createdTime DATETIME
);
CREATE UNIQUE INDEX ix_board_username ON board (username);
CREATE UNIQUE INDEX ix_board_anonymousId ON board (anonymousId);