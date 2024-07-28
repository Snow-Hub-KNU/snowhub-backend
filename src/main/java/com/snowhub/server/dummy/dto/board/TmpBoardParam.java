package com.snowhub.server.dummy.dto.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmpBoardParam {
    private int id;
    private String title;
    private String content;
    private String category;
}
