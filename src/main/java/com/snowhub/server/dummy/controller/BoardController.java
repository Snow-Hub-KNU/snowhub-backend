package com.snowhub.server.dummy.controller;

import com.snowhub.server.dummy.dto.board.BoardParam;
import com.snowhub.server.dummy.dto.board.BoardListDTO;
import com.snowhub.server.dummy.dto.reply.Board_ReplyDTO;
import com.snowhub.server.dummy.dto.reply.BoardDetail_ReplyDTO;
import com.snowhub.server.dummy.model.Board;

import com.snowhub.server.dummy.model.TmpBoard;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.repository.UserRepo;
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
    private final UserRepo userRepo;


    @PostMapping("/board/write")
    public ResponseEntity<?> boardWrite(@Valid @RequestBody BoardParam boardDTO,
                                        HttpServletRequest request) {
        log.debug("{}{}",boardDTO.getTitle(),boardDTO.getContent());

        return boardService.saveBoard(boardDTO,request);
    }

    @PostMapping("/board/tmp")
    public ResponseEntity<?> boardTmpSave(@RequestBody TmpBoard tmpBoard, HttpServletRequest request){
        // 임시저장을 하는데, 굳이 검사를 할 필요가 없다.
        return tmpBoardService.saveTmpBoard(tmpBoard,request);

    }

    @GetMapping("/board/tmp")
    public TmpBoard getTmpBoard(HttpServletRequest request){
        return tmpBoardService.getTmpBoard(request);
    }

    @GetMapping("/board/list")
    public ResponseEntity<?> boardList(@RequestParam int page,@RequestParam String category){

        List<BoardListDTO> boardListPack = new ArrayList<>();
        //
        PageRequest pageRequest=null;
        Page<Board> getPages = null;
        if(category.equals("all")){
            pageRequest = PageRequest.of(page,16, Sort.by("id").ascending());
            getPages = boardRepo.findAll(pageRequest);
        }
        else{
            pageRequest = PageRequest.of(page,16, Sort.by("id").ascending());
            getPages = boardRepo.findByCategory(category,pageRequest);

        }

        //PageRequest pageRequest2 = PageRequest.of(page,16);
        //Page<Board> tester = boardRepo.findByCategory("Season",pageRequest2);



        Iterator<Board> iterator = getPages.iterator();
        while(iterator.hasNext()){
           Board rawBoard = iterator.next();
           BoardListDTO boardList = BoardListDTO.builder()
                   .id(rawBoard.getId())
                   .category(rawBoard.getCategory())
                   .title(rawBoard.getTitle())
                   .writer(rawBoard.getUser().getDisplayName())
                   .count(rawBoard.getCount())
                   .createDate(rawBoard.getCreateDate())
                   .build();
            boardListPack.add(boardList);
        }

        // 게시판 전체 페이지를 넣기 위한 Dummy Data
        BoardListDTO totalPageDTO = new BoardListDTO();
        totalPageDTO.setId(getPages.getTotalPages());
        totalPageDTO.setTitle(getPages.getTotalPages()+"");

        boardListPack.add(totalPageDTO);


        // BoardDTO를 생성하지 않은 경우 -> StackOverFlow 발생.
        // 이유: json 내 obj 무한 참조.

        //
        String json = new Gson().toJson(boardListPack);

        return ResponseEntity.ok(json);
    }

    // /board/detail/{id} <- @PathVariable String id로 받기
    @GetMapping("/board/detail")
    public ResponseEntity<?> boardDetail(@RequestParam(name = "id") int boardId,
                                         @RequestParam(name = "number")int number){


        BoardListDTO board = boardService.getBoard(boardId);// 이거 안하면 Json으로 인해서 StackOverFlow 발생
        List<BoardDetail_ReplyDTO> reply = replyService.getReply(boardId);// 마찬가지이유.

        // Board와 Reply를 동시에 반환을 한다.
        Board_ReplyDTO boardReplyDTO = new Board_ReplyDTO();
        boardReplyDTO.setBoardDTO(board);
        boardReplyDTO.setReplyDTO(reply);

        //Board의 count +1을 한다.
        /*
            중복 랜더링 때문에...
            중복조회를 막기 위해서는 추후에 Redis를 활용할 것!
            쿠키를 이용할 경우, detail 들어갈 때마다 계속 1일로 초기화가 된다. 그러면 언제 만료가 되려나?
        */
        if(number==1){
            boardService.updateCount(boardId);
        }

        return ResponseEntity.ok(boardReplyDTO);// body T에는 board Entity가 들어가야.
    }

    @GetMapping("/dummy/check")
    public String doCheck(){
       List<User> users= userRepo.findAll();
       User user = users.get(0);
       List<Board> boardList = user.getBoardList();
       for(Board b: boardList){
           System.out.println("user: "+b.getUser().getEmail());
           System.out.println("board: "+b.getTitle());
       }

       return "OK";
    }

    @GetMapping("/dummy/board")
    public void fillDummyBoard(){

        User findUser = userRepo.findByEmail("tarto123z@gmail.com");
        for(int i=0; i<100; i++){
            Board board = new Board();


            board.setUser(findUser);
            board.setTitle("dummy"+i);
            board.setContent("test");
            board.setCategory("Season");

            boardRepo.save(board);
        }

    }

}

/*
1. RequestBody는 multipart/form-data를 json으로 전송해도 excpetion 발생 -> @RequestParam("image") MultipartFile file
   Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'multipart/form-data;boundary=----WebKitFormBoundary7bFMK5q5ziAvA0tq;charset=UTF-8' not supported ]

*/