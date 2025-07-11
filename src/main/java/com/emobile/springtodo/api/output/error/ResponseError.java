package com.emobile.springtodo.api.output.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder(toBuilder = true)
@Getter
public class ResponseError {
    private int status;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.S")
    private Timestamp timestamp;
}
