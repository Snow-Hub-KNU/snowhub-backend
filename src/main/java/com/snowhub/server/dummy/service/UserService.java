package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.repository.UserRepo;
import com.snowhub.server.dummy.model.RoleType;
import com.snowhub.server.dummy.model.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    // 1. 회원가입(Firebase)
    @Transactional
    public ResponseEntity<User> saveUser(User user){

        String rawPassword = user.getPassword();
        String encodePassword = passwordEncoder.encode(rawPassword);
        user.setRoleType(RoleType.user);

        user.setPassword(encodePassword);
        userRepo.save(user);


        ResponseEntity<User> responseEntity = new ResponseEntity<>(user, HttpStatus.OK);
        return responseEntity;
    }

    // 2. 회원가입(Google)
    @Transactional
    public void saveOAuthUser(User user){
        userRepo.save(user);
    }

    // 2. 이름기반으로 검색.
    @Transactional
    public User findUserByName(String username){
        return userRepo.findByDisplayName(username);
    }

    // 3. 이메일기반으로 검색.
    @Transactional
    public User findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // 4. refreshToken일치여부 찾기
    @Transactional
    public User findRefreshToken(String refeshToken){
        User findUser = userRepo.findByRefreshToken(refeshToken);
        return findUser;

    }

    // 5. 기존 refeshToken을 새로 업데이트.
    @Transactional
    public void updateRefreshToken(String refeshToken,String email){
        User findUser = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseGet(() -> {
                    // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                    throw new NullPointerException("Cannot update refreshToken. your not user!");
                });
        findUser.setRefreshToken(refeshToken);
    }

    // 6. uid 업데이트
    @Transactional
    public void updateUid(String email, String uid){
        User findUser = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseGet(() -> {
                    // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                    throw new NullPointerException("Cannot update refreshToken. your not user!");
                });
        findUser.setUid(uid);
    }

}
