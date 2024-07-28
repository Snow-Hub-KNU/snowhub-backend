package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.reply.ReplyParam;
import com.snowhub.server.dummy.dao.ReplyFetcher;
import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.repository.ReplyRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ReplyService {

    private final ReplyRepo replyRepo;
    private final BoardRepo boardRepo;

    // 1. 댓글 등록
    @Transactional
    public ResponseEntity<?> saveReply(ReplyParam replyParam){
        // replyDTO 이용해서 Board 찾기 -> reply 인스턴스에 초기화 -> Reply 등록
        int boardId = Integer.parseInt(replyParam.getBoardId());
        Board board = boardRepo.findById(boardId).orElseThrow(
                ()-> new RuntimeException("There is no ["+ replyParam.getBoardId()+"] Board" )
        );

        Reply reply = Reply.builder()
                .reply(replyParam.getReply())
                .board(board)
                .build();

        replyRepo.save(reply);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    // 2. Board Id 기반으로 Reply 가져오기
    @Transactional
    public List<ReplyFetcher> getReply(int boardId){
        Board board = boardRepo.findById(boardId).orElseThrow(
                ()-> new NullPointerException("Error:getReply : "+boardId)
        );

        List<Reply> replies =replyRepo.findAllByBoard(board);// 댓글이 안달릴수도 있으니까, Optional Null은 제외.
        List<ReplyFetcher> replyDTOList = new ArrayList<>();

        for(Reply r: replies){
            ReplyFetcher replyFetcher = new ReplyFetcher();
            replyFetcher.setId(r.getId());
            replyFetcher.setContent(r.getReply());
            replyFetcher.setName("익명");
            replyFetcher.setCreateDate(r.getCreateDate());

            replyDTOList.add(replyFetcher);
        }

        return replyDTOList;

    }
}
