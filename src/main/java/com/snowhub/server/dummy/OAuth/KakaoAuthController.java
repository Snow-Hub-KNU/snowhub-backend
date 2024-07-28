package com.snowhub.server.dummy.OAuth;

import com.snowhub.server.dummy.config.SupplierConfig.KakaoConfig;
import com.snowhub.server.dummy.dto.oauth.KaKao.KaKaoDetails;
import com.snowhub.server.dummy.dto.user.UserParam;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.module.CustomHttpClient;
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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@AllArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoConfig kakaoConfig;
    private final UserService userService;

    @GetMapping("/auth/kakao")
    public RedirectView redirect(@RequestParam String code,
                           HttpServletRequest req,
                           HttpServletResponse res){



        // Payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("grant_type","authorization_code");
        payload.add("client_id",kakaoConfig.getRestApiKey());
        payload.add("client_secret",kakaoConfig.getClientSecret());
        payload.add("redirect_uri",kakaoConfig.getRedirectUri());
        payload.add("code",code);


        KaKaoDetails.Token getTokenFromKaKao = CustomHttpClient.getInstance().mutate()
                .build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToMono(KaKaoDetails.Token.class)
                .block();

        //String tokenType = getTokenFromKaKao.getToken_type();
        String accessToken = getTokenFromKaKao.getAccess_token();
        //String idToken = getTokenFromKaKao.getId_token();
        //String refreshToken = getTokenFromKaKao.getRefresh_token();



        // 2. firebase의 Authentication에 등록

        // Payloads
        payload.clear();
        payload.add("property_keys", "[\"kakao_account.profile\",\"kakao_account.name\",\"kakao_account.email\"]");
        //

        KaKaoDetails.User getUserFromKaKao = CustomHttpClient.getInstance().mutate()
                .build()
                .post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization","Bearer "+accessToken)
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToMono(KaKaoDetails.User.class)
                .block();

        String email = getUserFromKaKao.getKakao_account().getEmail();// email
        String id = getUserFromKaKao.getId()+"";// password (고민)
        String name = getUserFromKaKao.getKakao_account().getProfile().getNickname();// username

        // 1. 신규 회원등록 로직
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        User getUser = userService.findUserByEmail(email);// email은 고유 식별자
        if (getUser==null){// 신규 회원등록 로직. 처음에만  save하고, 그 이후는 PASS
            try {
                log.info("Enroll new user!!!");
                // 신규 User를 추가.
                UserParam userDTO = UserParam.builder()
                        .email(email)
                        .password(id)
                        .displayName(name)
                        .build();

                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(userDTO.getEmail())
                        .setPassword(userDTO.getPassword())
                        .setDisplayName(userDTO.getDisplayName())// username
                        //.setEmailVerified(false)// 수정
                        //.setPhoneNumber(user.getPhoneNumber())
                        ;
                firebaseAuth.createUser(request);// 이미 등록된 사용자면 Exception발생
                userService.saveUser(userDTO);
            } catch (FirebaseAuthException e) {
                log.error("enroll original user double. this must not activated. if so, check here GoogleAuthController!!!");
                throw new RuntimeException(e.getMessage());
            }
        }


        // 2. idToken과 RefreshToken을 firebase로 부터 가져오기.

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

        JSONObject jsonObject3 = new JSONObject(getUserInfo);

        //String username = (String) jsonObject3.get("displayName");
        String idToken = (String) jsonObject3.get("idToken");
        String refreshToken = (String) jsonObject3.get("refreshToken"); // refreshToken은 DB에 저장.

        userService.updateRefreshToken(refreshToken,email);

        userService.updateRefreshToken(refreshToken,email);
        // Uid 재 업데이트
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            userService.updateUid(email,uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }


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
        return redirectView;

    }

}

/*
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // Payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("grant_type","authorization_code");
        payload.add("client_id",kakaoConfig.getRestApiKey());
        payload.add("client_secret",kakaoConfig.getClientSecret());
        payload.add("redirect_uri",kakaoConfig.getRedirectUri());
        payload.add("code",code);

        // RestTemplate
        RestTemplate rt = SingleRestTemplate.getInstance();

        // 1. 토큰 요청
        HttpEntity<MultiValueMap<String, String>> kakakoReq =
                new HttpEntity<>(payload, headers);

        ResponseEntity<String> response1 = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakakoReq,
                String.class
        );
        String getResponse=null;
        getResponse = response1.getBody();
        JSONObject jsonObject = new JSONObject(getResponse);
        //String tokenType = (String)jsonObject.get("token_type");
        String accessToken = (String)jsonObject.get("access_token");
        //String idToken = (String)jsonObject.get("id_token");
        //String refreshToken = (String)jsonObject.get("refresh_token");
        //System.out.println("accessToken: "+accessToken);
 */


/*
// Headers는 앞에 썼던 것 재사용.
        headers.add("Authorization","Bearer "+accessToken);

        // Payloads
        payload.clear();
        payload.add("property_keys", "[\"kakao_account.profile\",\"kakao_account.name\",\"kakao_account.email\"]");
        //
        HttpEntity<MultiValueMap<String, String>> kakakoReq2 =
                new HttpEntity<>(payload, headers);


        ResponseEntity<String> response2 = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakakoReq2,
                String.class
        );
        getResponse = response2.getBody();
        JSONObject jsonObject2 = new JSONObject(getResponse);

        //jsonObject2.get("connected_at"); <-DateTime
        JSONObject kakaoAccount = (JSONObject) jsonObject2.get("kakao_account");
        JSONObject jsonProfile = (JSONObject)kakaoAccount.get("profile");

        String email=(String) kakaoAccount.get("email");// email
        String id = (Long)jsonObject2.get("id")+"";// password (고민)
        String name = (String)jsonProfile.get("nickname");// username
 */