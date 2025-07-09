package com.emobile.springtodo.api.output.task;

import com.emobile.springtodo.core.entity.db.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class TaskResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TaskStatus status;
    private Timestamp start;
    private Timestamp deadline;
    private Timestamp end;
}
