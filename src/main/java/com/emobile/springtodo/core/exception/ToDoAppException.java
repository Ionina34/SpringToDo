package com.emobile.springtodo.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ToDoAppException extends RuntimeException {
    private Timestamp timestamp;

    public ToDoAppException(String message, Timestamp timestamp) {
        super(message);
        this.timestamp = timestamp;
    }
}
