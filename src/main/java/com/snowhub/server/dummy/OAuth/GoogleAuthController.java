package com.snowhub.server.dummy.OAuth;


import com.snowhub.server.dummy.config.CustomConfigurationSource;
import com.snowhub.server.dummy.config.SingleRestTemplate;

import com.snowhub.server.dummy.config.SupplierConfig.GoogleConfig;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;


@Slf4j
@AllArgsConstructor
@Controller
public class GoogleAuthController {

    //private static String warmup="init";

    private GoogleConfig googleConfig;
    private UserService userService;

    // 1. 이쪽으로 redirect가 됨. 여기서 accessToken,idToken 등 필요한 데이터를 가져온다.
    @GetMapping("/auth/google")
    public RedirectView reDirect(@RequestParam String code, RedirectAttributes redirectAttributes,
                           HttpServletRequest req, HttpServletResponse res){
        log.info("API = /auth/google ");

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // Payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("code", code);
        payload.add("client_id", googleConfig.getClientId());
        payload.add("client_secret", googleConfig.getClientSecret());
        payload.add("redirect_uri", googleConfig.getRedirectUri());
        payload.add("grant_type", "authorization_code");

        //HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> googleReq =
                new HttpEntity<>(payload, headers);

        //RestTemplate rt = new RestTemplate(); //http 요청을 간단하게 해줄 수 있는 클래스
        RestTemplate rt = SingleRestTemplate.getInstance();
        //실제로 요청하기
        //Http 요청하기 - POST 방식으로 - 그리고 response 변수의 응답을 받음.

        ResponseEntity<String> response = rt.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                googleReq,
                String.class
        );
        String getBody = response.getBody();
        JSONObject jsonObject = new JSONObject(getBody);

        String accessToken = (String)jsonObject.get("access_token");
        //int expiresIn = (Integer) jsonObject.get("expires_in");
        //String scope = (String)jsonObject.get("scope");
        //String tokenType = (String)jsonObject.get("token_type");
        // String idToken = (String)jsonObject.get("id_token");
        // String refreshToken = (String)jsonObject.get("refresh_token");

        // 1. 신규 유저인 경우, refresh 토큰과 함께 insert
        // 2. 기존 유저인 경우, DB에서 찾은 후 update

        //https://www.googleapis.com/oauth2/v2/userinfo <- id,이름,사진주소,국적

        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer " + accessToken);
        MultiValueMap<String, String> payload2 = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> googleReq2 =
                new HttpEntity<>(payload2, headers2);
        ResponseEntity<String> response2 = rt.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                googleReq2,
                String.class
        );
        String values = response2.getBody();
        JSONObject jsonObject1 = new JSONObject(values);

        String email = (String) jsonObject1.get("email");// email
        String id = (String) jsonObject1.get("id");// password (고민)
        String name = (String) jsonObject1.get("name");// username

        // 1. 신규 회원등록 로직
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        User getUser = userService.findUserByEmail(email);// email은 고유 식별자
        if (getUser==null){// 신규 회원등록 로직. 처음에만  save하고, 그 이후는 PASS
            try {
                log.info("Enroll new user!!!");
                // 신규 User를 추가.
                User user = User.builder()
                        .email(email)
                        .password(id)
                        .displayName(name)
                        .build();

                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(user.getEmail())
                        .setPassword(user.getPassword()) // 필수! 안하면 BAD_REQUEST
                        .setDisplayName(user.getDisplayName())// username
                        //.setEmailVerified(false)// 수정
                        //.setPhoneNumber(user.getPhoneNumber())
                        ;
                firebaseAuth.createUser(request);// 이미 등록된 사용자면 Exception발생
                userService.saveUser(user);
            } catch (FirebaseAuthException e) {
                log.error("enroll original user double. this must not activated. if so, check here GoogleAuthController!!!");
                throw new RuntimeException(e.getMessage());
            }
        }


        // 2. idToken과 RefreshToken을 firebase로 부터 가져오기.
        // 만약, 앞에 넣은 경우 사용자가 등록이 안되있는데, 가져올 수 없다.
        // 그래서, 추후에 다시 업로드를 해야한다.

        // WebClient URL, firebase로 API요청을 보내기 위해서.
        WebClient webClient = WebClient.create();

        // Body elements
        String returnSecureToken = "true";

        // Body Payload
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", email);
        formData.add("password", id);
        formData.add("returnSecureToken", returnSecureToken);

        // Firebase API를 이용해서, 로그인을 진행한다. <- 검증을 한다.
        // 그리고, idToken, refreshToken 받아오기.
        String getUserInfo = webClient.post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject jsonObject2 = new JSONObject(getUserInfo);

        //String username = (String) jsonObject2.get("displayName");
        String idToken = (String) jsonObject2.get("idToken");
        String refreshToken = (String) jsonObject2.get("refreshToken"); // refreshToken은 DB에 저장.

        userService.updateRefreshToken(refreshToken,email);
        // Uid 재 업데이트
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            userService.updateUid(email,uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        // Warmup의 위해서 따로 클래스를 생성해서 요청을 보내려고 했으나, 그럴려면 토큰이 필요하다.
        // 그러므로, 최초로 접속한 사람이 해당 API로 다중 요청을 보내도록하자.
        // 그 이후는 무시.


        // "http://localhost:8000/board/list"
        if(CustomConfigurationSource.warmup!=null){
            // 요청을 보낸다.
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers3 = new HttpHeaders();
            headers3.set("Authorization", "Bearer "+idToken);
            HttpEntity entity = new HttpEntity(headers3);


            String baseUrl = "http://localhost:8000/board/list";


            String uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("page", 0)
                    .queryParam("category", "all")
                    .build().toString();

            for(int i=0; i<100; i++) {
                ResponseEntity responseEntity = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class
                );
            }
            CustomConfigurationSource.warmup = null;// 일부러 null로 설정을 해서 더이상 참조 못하게 만들어서 minorGC하도록 설정을 하자

        }



        // setMaxAge를 할 경우, 브라우저를 종료해도 쿠키는 남아있는 상태가 된다.
        Cookie idCookie = new Cookie("IdTokenCookie",idToken);
        idCookie.setDomain("localhost");
        idCookie.setPath("/");
        //idCookie.setMaxAge(3600);
        idCookie.setSecure(false);


        Cookie refreshCookie = new Cookie("refreshTokenCookie",refreshToken);
        refreshCookie.setDomain("localhost");
        refreshCookie.setPath("/");
        //refreshCookie.setMaxAge(3600);
        refreshCookie.setSecure(false);

        res.addCookie(idCookie);
        res.addCookie(refreshCookie);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:3000/");

        log.info("API = /auth/google Completed");
        return redirectView;

    }
}
