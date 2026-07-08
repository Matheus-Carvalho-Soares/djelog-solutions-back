package com.djelog.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        String message,
        Map<String, String> fields,
        int status,
        String path
) {
}
