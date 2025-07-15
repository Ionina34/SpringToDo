package com.emobile.springtodo.api.output.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ListTaskResponse {
    List<TaskResponse> tasks;
    private long total;
    private int limit;
    private int offset;
}
