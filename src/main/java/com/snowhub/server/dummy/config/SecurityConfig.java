package com.snowhub.server.dummy.config;

import com.snowhub.server.dummy.config.SupplierConfig.CustomAccessDeniedHandler;
import com.snowhub.server.dummy.filter.VerifyTokenFilter;
import com.snowhub.server.dummy.service.security.CustomUserDetailsService;
import com.snowhub.server.dummy.service.UserService;
import com.snowhub.server.dummy.config.SupplierConfig.CustomAccessDeniedHandler;
import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
   private final CustomUserDetailsService customUserDetailsService;
   private final UserService userService;
   private final CustomAccessDeniedHandler customAccessDeniedHandler;
   private final CorsConfigurationSource corsConfigurationSource;
   
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(basic -> basic.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))//
                .csrf(csrf -> csrf.disable())//Csrf 안씀.
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// Session 안씀.
                .formLogin(login -> login.disable())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll() // 제일 의심가는 부분. <- 해결
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                .anyRequest().authenticated()
                        // ADMIN페이지는 따로 시큐리티 Authority를 부여. <- 토큰 로그인이 X
                )
                .addFilterBefore(new VerifyTokenFilter(customUserDetailsService,userService), UsernamePasswordAuthenticationFilter.class
                );

        http
                .exceptionHandling(exception -> exception.accessDeniedHandler(customAccessDeniedHandler)// 403에러
                );


        return http.build();
    }

    // 특정 URL은 VerifyTokenFilter를 거치지 않고 엑세스가 가능하다.

    // /auth/** 를 제외한 나머지는 삭제한다. ( 실행시간이 너무 길어서 )
    // auth 경로는 Token을 반환하므로, 필터링을 무시한다.

    @Bean WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring()
                .requestMatchers("/auth/**")
                ;
    }


}

