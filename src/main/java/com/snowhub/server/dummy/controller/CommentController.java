package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dao.CommentFetcher;
import com.snowhub.server.dummy.dto.comment.CommentParam;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.ReplyRepo;
import com.snowhub.server.dummy.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@AllArgsConstructor
@RestController
public class CommentController {

    private final ReplyRepo replyRepo;// 런타임 전에 오류를 잡기 위해서, 생성자 의존성 주입을 한다.
    private final CommentService commentService;

    @PostMapping("/board/comment")
    public ResponseEntity<?> getComment(@RequestParam(name = "id") int replyId,
                                        @RequestBody CommentParam commentParam){
        return commentService.saveComment(commentParam,replyId);

    }

    @GetMapping("/board/reply/comment")
    public ResponseEntity<?> getComment(@RequestParam(name = "id")int replyId){
        Reply reply = replyRepo.findById(replyId).orElseThrow(
                ()-> new NullPointerException("/board/reply/commment")
        );

        List<CommentFetcher> commentFetchers = commentService.getComment(reply);

        // reply로 comment에 관련 답글찾기
        return ResponseEntity.ok(commentFetchers);
    }

}
