package com.snowhub.server.dummy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration // 싱글톤
    public class PasswordEncoderConfig {
        @Bean
        public PasswordEncoder PasswordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

