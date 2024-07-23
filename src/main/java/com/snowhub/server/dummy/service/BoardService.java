package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.board.BoardParam;
import com.snowhub.server.dummy.dto.board.BoardListDTO;
import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.repository.UserRepo;
import com.snowhub.server.dummy.module.Firebase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class BoardService {

    private BoardRepo boardRepo;
    private UserRepo userRepo; // userRepo는 외부 빈 -> 순환참조 x

    @Transactional
    public ResponseEntity<?> saveBoard(BoardParam boardDTO, HttpServletRequest request){

        log.info("BoardService/saveBoard Start");
        // 1. 이미 앞서 검증이 완료된 Board (by boardDTO), 만약 에러 발생시 GlobalExceptionHandler에서 캐치 후 에러 리턴.

        Board board = new Board();

        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setCategory(boardDTO.getCategory());


        Firebase firebase = Firebase.getInstance();
        String userEmail = firebase.getEmail(request);


        User findUser = Optional.ofNullable(userRepo.findByEmail(userEmail)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더?

        // 3. user 찾은 후, 저장하기
        findUser.addBoards(board); // user dirty checking???
        board.setUser(findUser);

        boardRepo.save(board);

        log.info("BoardService/saveBoard Completed");
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Transactional
    public BoardListDTO getBoard(int id){
        Optional<Board> optionalBoard = boardRepo.findById(id);
        Board board = optionalBoard.orElseGet(
                ()->{ throw new NullPointerException("There is no board");}
        );

        BoardListDTO boardDTO = BoardListDTO.builder()
                .id(board.getId())
                //.count(board.getCount()) 추후에 추가할 예정
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getDisplayName())
                .category(board.getCategory())
                .createDate(board.getCreateDate())
                .build();

        return boardDTO;
    }

    @Transactional
    public void updateCount(int boardId){
        // board 가져오기 -> 원본을 변경 -> orig와 변경된 board 서로 compare -> Dirty Checking에 의해서 자동 update
        Board board = boardRepo.findById(boardId).orElseGet(
                ()->{
                    throw new NullPointerException("Can't find board:"+boardId);
                }
        );
        System.out.println("boardService/updateCount");
        board.setCount(board.getCount()+1);

    }
}



// 2. request 헤더에서 토큰 추출 -> email 추출 ->

        /*
        String getToken = request.getHeader("Authorization");
        String bearerToken=null;
        if (getToken != null && getToken.startsWith("Bearer ")) {
            bearerToken = getToken.substring(7); // Extracting the token after "Bearer "
        }

        else{
            throw new RuntimeException("It is not Bearer Token");
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseToken decode = null;
        // Throw를 할 경우, boardService를 쓰는 모든 컴포넌트가 throw FirebaseException을 throw 해야함.
        try {
            decode =  firebaseAuth.verifyIdToken(bearerToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        String userEmail = decode.getEmail();
        */