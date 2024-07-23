package com.snowhub.server.dummy.config.SupplierConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("=====[ CustomAccessDeniedHandler executed! ]=====");
        // accessToken에 문제가 발생한 모든 경우.
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject data = new JSONObject();
        data.put("error","You can't access this page!. because you don't have authority");
        data.put("status",HttpStatus.FORBIDDEN.value());

        response.getWriter().write(data.toString());

    }
}
