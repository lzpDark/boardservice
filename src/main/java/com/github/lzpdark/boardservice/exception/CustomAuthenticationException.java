package com.github.lzpdark.boardservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author lzp
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class CustomAuthenticationException extends AuthenticationException {
   public CustomAuthenticationException(String message) {
       super(message);
   }
}
