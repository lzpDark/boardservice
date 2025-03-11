package com.github.lzpdark.boardservice.exception;

/**
 * @author lzp
 */
public abstract class BusinessException extends Exception {
    public BusinessException(String message) {
        super(message);
    }
}
