package com.github.lzpdark.boardservice.model;

import lombok.Data;

import java.sql.Date;

/**
 * @author lzp
 */
@Data
public class AnonymousUser {
    private int id;
    private Date createdTime;
}
