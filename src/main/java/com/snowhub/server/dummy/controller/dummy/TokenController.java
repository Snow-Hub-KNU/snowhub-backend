package com.snowhub.server.dummy.controller.dummy;

import com.snowhub.server.dummy.exceptionHandler.FirebaseError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

// Token에서 발생한 예외상황에 대한 리다이렉트 처리를 위한 컨트롤러.
// 다른 용도로 사용x
@Slf4j
@Controller
public class TokenController {

    @GetMapping("/redirect/login")
    public RedirectView goToLoginPage(){
        log.info("Redirect login page form Token_Controller");
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:3000/login");

        throw new FirebaseError("no");

    }
}
