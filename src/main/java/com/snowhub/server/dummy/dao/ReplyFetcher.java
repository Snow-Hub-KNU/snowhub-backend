package com.snowhub.server.dummy.dao;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ReplyFetcher {
    private int id;
    private String name;
    private String content;
    private Timestamp createDate;

}
