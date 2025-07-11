package com.emobile.springtodo.core.exception;

import java.sql.Timestamp;

public class AccessRightsException extends ToDoAppException{
    public AccessRightsException(String message, Timestamp timestamp) {
        super(message, timestamp);
    }
}
