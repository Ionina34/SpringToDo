package com.emobile.springtodo.core.entity.db;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO, IN_PROGRESS, DONE;

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
