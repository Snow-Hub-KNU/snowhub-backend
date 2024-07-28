package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.board.TmpBoardParam;
import com.snowhub.server.dummy.model.TmpBoard;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.module.Firebase;
import com.snowhub.server.dummy.repository.TmpBoardRepo;
import com.snowhub.server.dummy.repository.UserRepo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class TmpBoardService {

    private final UserRepo userRepo;

    private final TmpBoardRepo tmpBoardRepo; // 생성자 주입을 해야하는 이유: 컴파일때 오류를 찾을 수 있음.
    @Transactional
    public ResponseEntity<?> saveTmpBoard(TmpBoardParam tmpBoardParam, HttpServletRequest request){
        // tmpBoard 먼저 저장 -> User에 tmpBoard 저장(더티체킹)
        // TmpBoard를 만든 이유: 혹시나 모를 에러체킹을 위해서 DTO생성.

        // 2. request 헤더에서 토큰 추출 -> email 추출 ->
        Firebase firebase = Firebase.getInstance();
        String email = firebase.getEmail(request);
        
        User findUser = Optional.ofNullable(userRepo.findByEmail(email)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더

        // 처음 저장 -> tmpBoard(save), 그 이후는 -> tmpBoard(update)

        TmpBoard inputTmpBoard = new TmpBoard();
        inputTmpBoard.setTitle(tmpBoardParam.getTitle());
        inputTmpBoard.setContent(inputTmpBoard.getContent());
        inputTmpBoard.setCategory(tmpBoardParam.getCategory());

        TmpBoard TmpBoardFetch  = findUser.getTmpBoard(); // Dirty checking을 위해서, TmpBoard객체를 불러옴.

        if(TmpBoardFetch==null){
            // Null인 경우 -> 첫등록
            tmpBoardRepo.save(inputTmpBoard);
            findUser.setTmpBoard(inputTmpBoard);
        }
        else {
            // Null이 아닌 경우 -> 이미 있는 경우
            // tmpBoard만 update를 하자.
            TmpBoardFetch.setTitle(inputTmpBoard.getTitle());
            TmpBoardFetch.setContent(inputTmpBoard.getContent());
            TmpBoardFetch.setCategory(inputTmpBoard.getCategory());
        }

        return ResponseEntity.ok("OK");
    }

    @Transactional
    public TmpBoard getTmpBoard(HttpServletRequest request){

        Firebase firebase = Firebase.getInstance();
        String email = firebase.getEmail(request);

        User findUser = Optional.ofNullable(userRepo.findByEmail(email)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더?

        return findUser.getTmpBoard();

    }
}
