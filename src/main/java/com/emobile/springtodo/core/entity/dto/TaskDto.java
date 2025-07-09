package com.emobile.springtodo.core.entity.dto;

import com.emobile.springtodo.core.entity.db.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
public class TaskDto {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TaskStatus status;
    private Timestamp start;
    private Timestamp deadline;
    private Timestamp end;
}
