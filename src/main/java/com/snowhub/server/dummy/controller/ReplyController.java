package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dto.reply.ReplyDTO;
import com.snowhub.server.dummy.service.ReplyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
public class ReplyController {

    // 컴파일 때 에러를 잡기 위해서 생성자 주입 방식을 선택.
    private final ReplyService replyService;

    // 1. Reply 등록하기.
    @PostMapping("/board/reply")
    public ResponseEntity<?> getReply(@RequestBody ReplyDTO replyDTO){
        return replyService.saveReply(replyDTO);
    }

}
