package com.snowhub.server.dummy.exceptionHandler;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();

        StringBuilder stringBuilder = new StringBuilder();
        for(FieldError fieldError: ex.getFieldErrors()){
            stringBuilder.append(fieldError.getDefaultMessage());
        }

        String errorMsg = stringBuilder.toString();
        ResponseEntity<?> responseError = ResponseEntity.badRequest().body(errorMsg);


        return handleExceptionInternal(ex, responseError, headers, status, request);
    }

}
