package com.emobile.springtodo.core.exception;

import java.sql.Timestamp;

public class ObjectNotFoundException extends ToDoAppException {
    public ObjectNotFoundException(String message, Timestamp timestamp) {
        super(message, timestamp);
    }
}
