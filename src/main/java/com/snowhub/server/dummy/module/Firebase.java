package com.snowhub.server.dummy.module;

import com.snowhub.server.dummy.config.SingleRestTemplate;
import com.snowhub.server.dummy.filter.tokenError.ErrorType;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.service.security.CustomUserDetailsService;
import com.snowhub.server.dummy.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
// 싱글톤으로 생성
public class Firebase {

    // 1. 변수 초기화

    // Service는 Bean 주입을 하자.
    private CustomUserDetailsService customUserDetailsService;
    private UserService userService;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // 2. 싱글톤 패턴
    private Firebase(){

    }
    private static class SingleInstanceHolder{
        private static final Firebase INSTANCE = new Firebase();

    }

    public static Firebase getInstance(){
        return SingleInstanceHolder.INSTANCE;
    }


    // 1. UserDetailService 초기화

    public void setCustomUserDetailsService(CustomUserDetailsService customUserDetailsService,
                                            UserService userService){
        this.customUserDetailsService=customUserDetailsService;
        this.userService=userService;
    }


    //
    // 1. 토큰 검증 메서드.
    public void verifyToken(HttpServletRequest request,
                            HttpServletResponse response,
                            String accessToken, String RefreshToken,
                            FilterChain filterChain) throws IOException {

        // 토큰 검증 로직
        try {
            // 1. accessToken 검증로직
            log.info("1. Decode AccessToken");
            FirebaseToken decode = firebaseAuth.verifyIdToken(accessToken);
            String email = decode.getEmail();// 회원을 구분짓는 username 대신의 식별자.
            //String username =decode.getName();
            log.info("2. Make Authentication & Save at ContextHolder");


            UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("3. PASS Filter");

            filterChain.doFilter(request,response);
            return;

        } catch (FirebaseAuthException | IOException | ServletException e) { // 강제로 예외 발생안시키고, 다음꺼 실행
            // 2. 토큰 오류 처리와 로직

            if(e.getMessage().equals(ErrorType.Not_Valid_Token.getValue())||
                    e.getMessage().equals(ErrorType.Parse_Error.getValue())
            ){
                // 1. 이상한 토큰을 받은 경우.
                handleException(response,e);
                return;
            }
            else if(e.getMessage().equals(ErrorType.Token_Expiration.getValue()) && RefreshToken==null ){
                // 2. 만료된 firebase 토큰 + RefreshToken NULL
                // 프론트엔드로 해당 오류 전송 -> 리액트에서 해당 오류에 맞게 accessToken과 refreshToken 함께 재전송
                handleException(response,e);
                return;
            }

        }

        // NOT_FireBaseToken인 경우 -> 무조건 로그인 페이지로 redirect

        // 2. Firebase의 refeshToken인지 검증. 실패시 status는 false로 종료.
        try{
            log.info("3. Check RefreshToken");
            // Header Setting
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // Payload Setting
            MultiValueMap<String,String> payload = new LinkedMultiValueMap<>();
            payload.add("grant_type","refresh_token");
            payload.add("refresh_token",RefreshToken);


            RestTemplate rt = SingleRestTemplate.getInstance();
            // Header + Payload
            HttpEntity<MultiValueMap<String, String>> firebaseReq =
                    new HttpEntity<>(payload, headers);


            ResponseEntity<String> firebaseResponse = rt.exchange(
                    "https://securetoken.googleapis.com/v1/token?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA",
                    HttpMethod.POST,
                    firebaseReq,
                    String.class
            );

            String getData = firebaseResponse.getBody();
            JSONObject jsonObject = new JSONObject(getData);

            // 기존 회원의 refreshToken을 갱신해준다.
            User findUser = userService.findRefreshToken(RefreshToken);// refreshToken으로 User찾기.
            String email = findUser.getEmail();
            //String username = findUser.getDisplayName();

            String idToken = (String)jsonObject.get("id_token");
            //String refreshToken = (String)jsonObject.get("refresh_token");

            //userService.updateRefreshToken(refreshToken,email);// 기존 refreshToken을 새로 교체 -> 원래 refreshTokne 유지!


            // Authentication을 등록한다.
            UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);


            //굳이 request에 실어서 보내지 말고, 여기서 response의 header에 담아서 처리하자.

            response.addHeader("accesstoken",idToken);
            response.addHeader("refreshtoken",RefreshToken);
            log.info("4. RefreshToken Check Completed");
            filterChain.doFilter(request,response);

        }
        catch (Exception e){
            handleException(response,e);
        }

    }

    // 2. 예외 처리 메서드
    private void handleException(HttpServletResponse response,Exception e) throws IOException {

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String,String> responseBody = new HashMap<>();
        responseBody.put("error",e.getMessage());
        responseBody.put("status",HttpStatus.BAD_REQUEST.value()+"");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(responseBody);

        response.getWriter().write(json);

    }

    // 3. 이메일 추출 메서드
    public String getEmail(HttpServletRequest request){

        String getBearerToken = request.getHeader("Authorization");
        String accesstoken=null;

        if (getBearerToken != null && getBearerToken.startsWith("Bearer ")) {
            accesstoken = getBearerToken.substring(7); // Extracting the token after "Bearer "
        }

        else{
            // Bearer로 시작하는 토큰이 아닌 경우 -> 오류발생
            throw new RuntimeException("It is not Bearer Token");
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseToken decode = null;
        // Throw를 할 경우, boardService를 쓰는 모든 컴포넌트가 throw FirebaseException을 throw 해야함.
        try {
            decode =  firebaseAuth.verifyIdToken(accesstoken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        return decode.getEmail();

    }

}

/*

   1. 기존 싱글톤(Thread Not Safe)
   private static Firebase firebase;

   public static Firebase getInstance(){
        if(firebase==null){
            firebase = new Firebase();
            return firebase;
        }
        return firebase;
    }
 */



/*
     2. 오류 처리 로직
     response.setStatus(HttpStatus.BAD_REQUEST.value());
     response.setContentType("application/json");
     response.setCharacterEncoding("UTF-8");

     Map<String,String> responseBody = new HashMap<>();
     responseBody.put("error",e.getMessage());
     responseBody.put("status",HttpStatus.BAD_REQUEST.value()+"");

     ObjectMapper objectMapper = new ObjectMapper();
     String json = objectMapper.writeValueAsString(responseBody);

     response.getWriter().write(json);
     return;
 */


// 예외처리
/*
    1. response.redirect() -> 결과 204발생
    이유: response.redirect 설정시, status와 header 2개만 설정, old 요청은 싹다 사라짐. 그래서 response body는 빈 상태.
    게다가, 브라우저에서 서버로 response url에 맞춰서 request를 하는데, 나는 서버가 아닌 프론트에서 요청을 보내려는데, 이러면 안됨.

    2. redirectView -> 반환 불가로 실패

    3, GlobalExceptionHandler로 처리 <- 얘는 오직 Controller에서 처리한 예외만 인식가능함. 커스텀 필터는 컨트롤러가 아니므로, 당연히 안됨.

    -> sol
    response에 에러 셋팅을 해서 보내는 방법 말고는 없어보인다.
*/