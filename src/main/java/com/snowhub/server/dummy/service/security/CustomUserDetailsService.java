package com.snowhub.server.dummy.service.security;

import com.snowhub.server.dummy.repository.UserRepo;
import lombok.AllArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


// 회원가입한 사람들 모두 USER인데 굳이 권한을 부여할 필요가 X.


@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepo userRepo;

    private PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UsernameNotFoundException이  발생.
        // 굳이 handler만들 필요 ???
        // User클래스가 겹침. <- 수정

        com.snowhub.server.dummy.model.User user
                = userRepo.findByDisplayName(username);
        String role = user.getRoleType().name();

        // authorities에 무조건 "ROLE_"+role을 넣어야 페이지에 대한 접근권한 유효.
        return User.builder()
                .roles(role)
                .username(user.getDisplayName())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_"+role))// 중요, 추후 여러 권한 부여가 가능하도록 수정.
                .build();
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {

        com.snowhub.server.dummy.model.User user
                = userRepo.findByEmail(email);
        String role = user.getRoleType().name();

        // authorities에 무조건 "ROLE_"+role을 넣어야 페이지에 대한 접근권한 유효.
        return User.builder()
                .roles(role)
                .username(user.getDisplayName())
                .password(user.getPassword())// 추후, 사용자가 입력한 비밀번호를 Bcrypt한 후 서로 같은지 비교
                .authorities(new SimpleGrantedAuthority("ROLE_"+role))// 중요
                .build();
    }

}

