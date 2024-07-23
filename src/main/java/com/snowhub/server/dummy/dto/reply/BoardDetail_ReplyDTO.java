package com.snowhub.server.dummy.dto.reply;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class BoardDetail_ReplyDTO {
    private int id;
    private String name;
    private String content;
    private Timestamp createDate;

}
