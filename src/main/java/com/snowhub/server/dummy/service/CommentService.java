package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dao.CommentFetcher;
import com.snowhub.server.dummy.dto.comment.CommentParam;
import com.snowhub.server.dummy.model.Comment;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.CommentRepo;
import com.snowhub.server.dummy.repository.ReplyRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class CommentService {
    private final ReplyRepo replyRepo;
    private final CommentRepo commentRepo;

    // 1.Comment등록하기
    @Transactional
    public ResponseEntity<?> saveComment(CommentParam commentParam, int replyId){
        // save comment, dirty checking about reply

        // Reply 찾기
        Reply reply = replyRepo.findById(replyId).orElseThrow(
                ()-> new NullPointerException("Cannot Find reply:"+replyId)
        );

        // Comment 객체 생성
        Comment comment = new Comment();
        comment.setComment(commentParam.getComment());
        comment.setReply(reply);

        commentRepo.save(comment);

        return ResponseEntity.ok("Good comment");

    }

    // 2. 특정 reply에 대한 comment 가져오기
    public List<CommentFetcher> getComment(Reply reply){
        List<Comment> commentList = commentRepo.findByReply(reply);

        List<CommentFetcher> returnComments = new ArrayList<>();

        // DTO LIST로 만들기
        for(Comment c: commentList){
            CommentFetcher commentFetcher = new CommentFetcher();
            commentFetcher.setId(c.getId());
            commentFetcher.setComment(c.getComment());
            returnComments.add(commentFetcher);
        }

        return returnComments;
    }
}
