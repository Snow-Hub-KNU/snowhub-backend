package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.comment.CommentDTO;
import com.snowhub.server.dummy.model.Comment;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.CommentRepo;
import com.snowhub.server.dummy.repository.ReplyRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.C;
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
    public ResponseEntity<?> saveComment(CommentDTO commentDTO, int replyId){
        // save comment, dirty checking about reply

        // Reply 찾기
        Reply reply = replyRepo.findById(replyId).orElseThrow(
                ()-> new NullPointerException("Cannot Find reply:"+replyId)
        );

        // Comment 객체 생성
        Comment comment = new Comment();
        comment.setComment(commentDTO.getComment());
        comment.setReply(reply);

        commentRepo.save(comment);

        return ResponseEntity.ok("Good comment");

    }

    // 2. 특정 reply에 대한 comment 가져오기
    public List<CommentDTO> getComment(Reply reply){
        List<Comment> comment = commentRepo.findByReply(reply);

        List<CommentDTO> returnValue = new ArrayList<>();

        // DTO LIST로 만들기
        for(Comment c: comment){
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(c.getId());
            commentDTO.setComment(c.getComment());
            returnValue.add(commentDTO);
        }

        return returnValue;
    }
}
