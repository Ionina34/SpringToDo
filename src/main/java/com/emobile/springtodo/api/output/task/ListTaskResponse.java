package com.emobile.springtodo.api.output.task;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ListTaskResponse {
    List<TaskResponse> tasks;
}
