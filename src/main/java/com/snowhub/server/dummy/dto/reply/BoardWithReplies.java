package com.snowhub.server.dummy.dto.reply;

import com.snowhub.server.dummy.dao.BoardFetcher;
import com.snowhub.server.dummy.dao.ReplyFetcher;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardWithReplies {
    private BoardFetcher boardDTO;
    private List<ReplyFetcher> replyDTO;

}
