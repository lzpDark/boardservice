package com.github.lzpdark.boardservice.exception;

import com.github.lzpdark.boardservice.common.CommonErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author lzp
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RegisterException.class)
    public ResponseEntity<?> handleBusinessException(RegisterException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CommonErrorResponse(e.getMessage(), e.getErrors()));
    }
}
