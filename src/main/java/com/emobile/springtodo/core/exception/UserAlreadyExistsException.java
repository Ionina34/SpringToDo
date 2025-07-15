package com.emobile.springtodo.core.exception;

import java.sql.Timestamp;

public class UserAlreadyExistsException extends ToDoAppException{
    public UserAlreadyExistsException(String message, Timestamp timestamp) {
        super(message, timestamp);
    }
}
