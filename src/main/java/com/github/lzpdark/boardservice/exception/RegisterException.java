package com.github.lzpdark.boardservice.exception;

import lombok.Getter;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * @author lzp
 */
@Getter
public class RegisterException extends BusinessException{

    private final Map<String ,String> errors;

    public RegisterException(String message, Map<String ,String> errors) {
        super(message);
        this.errors = errors;
    }
}
