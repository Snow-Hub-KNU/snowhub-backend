package com.snowhub.server.dummy.dto.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyDTO {

    // 게시글 고유 넘버.
    private String boardId;

    @NotBlank(message = "The reply is Empty")
    private String reply;
}
