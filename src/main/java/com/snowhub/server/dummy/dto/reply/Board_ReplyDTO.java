package com.snowhub.server.dummy.dto.reply;

import com.snowhub.server.dummy.dto.board.BoardListDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Board_ReplyDTO {
    private BoardListDTO boardDTO;
    private List<BoardDetail_ReplyDTO> replyDTO;

}
