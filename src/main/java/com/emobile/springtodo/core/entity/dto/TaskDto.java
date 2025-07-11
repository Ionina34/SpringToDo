package com.emobile.springtodo.core.entity.dto;

import com.emobile.springtodo.core.entity.db.TaskStatus;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TaskDto implements Serializable {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TaskStatus status;
    private Timestamp start;
    private Timestamp deadline;
    private Timestamp end;
}
