package com.snowhub.server.dummy.service;

import com.snowhub.server.dummy.dto.user.UserParam;
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
    public void saveUser(UserParam userDTO){

        String rawPassword = userDTO.getPassword();
        String encodePassword = passwordEncoder.encode(rawPassword);
        userDTO.setRoleType(RoleType.user);

        userDTO.setPassword(encodePassword);

        User user = User.builder()
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .displayName(userDTO.getDisplayName())
                .roleType(userDTO.getRoleType())
                .build()
                ;
        userRepo.save(user);

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
        return userRepo.findByRefreshToken(refeshToken);

    }

    // 5. 기존 refeshToken을 새로 업데이트.
    @Transactional
    public void updateRefreshToken(String refeshToken,String email){
        User findUser = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseThrow(() -> // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                        new NullPointerException("Cannot update refreshToken. your not user!"));
        findUser.setRefreshToken(refeshToken);
    }

    // 6. uid 업데이트
    @Transactional
    public void updateUid(String email, String uid){
        User findUser = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseThrow(() -> // 일치하는 email이 없는 경우 등록된 사용자가 x -> 에러발생.
                        new NullPointerException("Cannot update refreshToken. your not user!"));
        findUser.setUid(uid);
    }

}
