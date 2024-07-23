package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.model.TmpBoard;
import com.snowhub.server.dummy.model.User;
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
    public ResponseEntity<?> saveTmpBoard(TmpBoard tmpBoard,HttpServletRequest request){
        // tmpBoard 먼저 저장 -> User에 tmpBoard 저장(더티체킹)

        // 2. request 헤더에서 토큰 추출 -> email 추출 ->
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
        User findUser = Optional.ofNullable(userRepo.findByEmail(userEmail)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더?

        // tmpBoar에 처음 -> save, 그 다음은 -> upate
        // 안쓰면 delete 굳이? 그러면 또 insert해야하는데, 차라리 다하고 NULL로 바꾸자.


        System.out.println("TmpBoarService/saveTmpBoard");

        // 처음 저장 -> tmpBoard(save), findUser(update)
        // 그 이후? -> tmpBoard(update), findUser는 Don't care <- 어차피 관계만 맺으려고 설정한 것임.
        System.out.println("Error!");

        TmpBoard tmp = findUser.getTmpBoard();

        if(tmp==null){
            // Null인 경우
            System.out.println("첫등록");
            tmpBoardRepo.save(tmpBoard);
            findUser.setTmpBoard(tmpBoard);
        }
        else {
            // Null이 아닌 경우 -> 이미 있는 경우
            // tmpBoard만 update를 하자.
            System.out.println("나중등록");
            tmp.setTitle(tmpBoard.getTitle());
            tmp.setContent(tmpBoard.getContent());
            tmp.setCategory(tmpBoard.getCategory());

        }

        /*
        int tmpBoardId = findUser.getTmpBoard().getId();
        System.out.println("tmpBoardId: "+tmpBoardId);
        Optional<TmpBoard> getTmpBoard = tmpBoardRepo.findById(tmpBoardId);
        if(getTmpBoard.isEmpty()){
            // Null인 경우
            System.out.println("첫등록");
            tmpBoardRepo.save(tmpBoard);
            findUser.setTmpBoard(tmpBoard);
        }
        else{
            // Null이 아닌 경우 -> 이미 있는 경우
            // tmpBoard만 update를 하자.
            System.out.println("나중등록");
            TmpBoard target = getTmpBoard.get();
            target.setTitle(tmpBoard.getTitle());
            target.setContent(tmpBoard.getContent());
            target.setCategory(tmpBoard.getCategory());
        }
         */

        return ResponseEntity.ok("OK");
    }

    @Transactional
    public TmpBoard getTmpBoard(HttpServletRequest request){
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
        User findUser = Optional.ofNullable(userRepo.findByEmail(userEmail)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더?

        return findUser.getTmpBoard();

    }
}
