package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dto.board.BoardParam;
import com.snowhub.server.dummy.dao.BoardFetcher;
import com.snowhub.server.dummy.dto.board.TmpBoardParam;
import com.snowhub.server.dummy.dto.reply.BoardWithReplies;
import com.snowhub.server.dummy.dao.ReplyFetcher;
import com.snowhub.server.dummy.model.Board;

import com.snowhub.server.dummy.model.TmpBoard;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.service.BoardService;
import com.snowhub.server.dummy.service.ReplyService;
import com.snowhub.server.dummy.service.TmpBoardService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.*;


@Slf4j
@AllArgsConstructor
@RestController
public class BoardController {

    // 생성자 주입 방식.
    private final BoardService boardService;
    private final ReplyService replyService;
    private final TmpBoardService tmpBoardService;

    private final BoardRepo boardRepo;

    @PostMapping("/board/write")
    public ResponseEntity<?> boardWrite(@Valid @RequestBody BoardParam boardDTO,
                                        HttpServletRequest request) {
        log.debug("{}{}",boardDTO.getTitle(),boardDTO.getContent());

        return boardService.saveBoard(boardDTO,request);
    }

    // - 게시글 임시 저장하기
    @PostMapping("/board/tmp")
    public ResponseEntity<?> boardTmpSave(@RequestBody TmpBoardParam tmpBoardParam, HttpServletRequest request){
        // 임시저장을 하는데, 굳이 검사를 할 필요가 없다.
        return tmpBoardService.saveTmpBoard(tmpBoardParam,request);

    }

    // - 임시 저장된 게시글 불러오기
    @GetMapping("/board/tmp")
    public TmpBoard getTmpBoard(HttpServletRequest request){
        return tmpBoardService.getTmpBoard(request);
    }

    // - 게시글 불러오기
    @GetMapping("/board/list")
    public ResponseEntity<?> boardList(@RequestParam int page,@RequestParam String category){

        Page<Board> pages;

        if(category.equals("all")){
            // 카테고리 구분없이 id 오름차순으로 가져오기 <- 가장 최신순으로 
            pages = boardRepo.findAll(PageRequest.of(page,16, Sort.by("id").descending()));
        }
        else{
            // 카테고리 별로 + id 오름차순으로 가져오기 <- 가장 최신순으로
            pages = boardRepo.findByCategory(category,PageRequest.of(page,16, Sort.by("id").descending()));
        }

        List<BoardFetcher> returnBoards = new ArrayList<>(pages.stream()
                .map(   // param = Board, return BoadListDTO
                        (e) -> BoardFetcher.builder()
                                .id(e.getId())
                                .category(e.getCategory())
                                .title(e.getTitle())
                                .writer(e.getUser().getDisplayName())
                                .count(e.getCount())
                                .createDate(e.getCreateDate())
                                .build()
                )
                .toList())
        ;
        
        // 전체 페이지 개수
        BoardFetcher pageSizeEntity = new BoardFetcher();
        pageSizeEntity.setId(pages.getTotalPages());
        pageSizeEntity.setTitle(pages.getTotalPages()+"");

        returnBoards.add(pageSizeEntity);

        String json = new Gson().toJson(returnBoards);

        return ResponseEntity.ok(json);
    }

    // /board/detail/{id} <- @PathVariable String id로 받기
    @GetMapping("/board/detail")
    public ResponseEntity<?> boardDetail(@RequestParam(name = "id") int boardId,
                                         @RequestParam(name = "number")int number){

        BoardFetcher fetchBoard = boardService.getBoard(boardId);// 이거 안하면 Json으로 인해서 StackOverFlow 발생
        List<ReplyFetcher> fetchReplies = replyService.getReply(boardId);// 마찬가지이유.

        // Board와 Reply를 동시에 반환을 한다.
        BoardWithReplies boardWithRepliesDTO = new BoardWithReplies();
        boardWithRepliesDTO.setBoardDTO(fetchBoard);
        boardWithRepliesDTO.setReplyDTO(fetchReplies);

        //Board의 count +1을 한다.
        /*
            중복 랜더링 때문에...
            중복조회를 막기 위해서는 추후에 Redis를 활용할 것!
            쿠키를 이용할 경우, detail 들어갈 때마다 계속 1일로 초기화가 된다. 그러면 언제 만료가 되려나?
        */
        if(number==1){
            boardService.updateCount(boardId);
        }

        return ResponseEntity.ok(boardWithRepliesDTO);// body T에는 board Entity가 들어가야.
    }
}

/*
1. RequestBody는 multipart/form-data를 json으로 전송해도 excpetion 발생 -> @RequestParam("image") MultipartFile file
   Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'multipart/form-data;boundary=----WebKitFormBoundary7bFMK5q5ziAvA0tq;charset=UTF-8' not supported ]

*/