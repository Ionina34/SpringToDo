package com.emobile.springtodo.api.output.error;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder(toBuilder = true)
@Getter
public class ResponseError {
    private int status;
    private String message;
    private Instant timestamp;
}
