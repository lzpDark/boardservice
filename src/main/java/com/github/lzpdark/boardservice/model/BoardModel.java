package com.github.lzpdark.boardservice.model;

import lombok.Data;

import java.sql.Date;

/**
 * @author lzp
 */
@Data
public class BoardModel {
    private int id;
    private String username;
    private String anonymousId;
    private String description;
    private Date createdTime;
}
