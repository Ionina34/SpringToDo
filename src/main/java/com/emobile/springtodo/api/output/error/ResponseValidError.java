package com.emobile.springtodo.api.output.error;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResponseValidError extends ResponseError {
    private List<ValidError> errors;

   public ResponseValidError(int status, String message, Instant timestamp, List<ValidError> errors) {
        super(status, message, timestamp);
        this.errors = errors;
    }
}
