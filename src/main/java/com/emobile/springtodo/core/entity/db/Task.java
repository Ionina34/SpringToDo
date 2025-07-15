package com.emobile.springtodo.core.entity.db;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TaskStatus status;
    private Timestamp deadline;
    private Timestamp createdAt;
    private Timestamp endData;
}
