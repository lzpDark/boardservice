package com.github.lzpdark.boardservice.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @author lzp
 */
@Data
@AllArgsConstructor
public class CommonErrorResponse {

    private String message; // TODO: define most business error exposed to user
    private Map<String, String> errors;
}
