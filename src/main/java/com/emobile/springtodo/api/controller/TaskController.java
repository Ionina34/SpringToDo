package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
import com.emobile.springtodo.api.output.ApiResponse;
import com.emobile.springtodo.api.output.task.ListTaskResponse;
import com.emobile.springtodo.api.output.task.TaskResponse;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.AccessRightsException;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/todo/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ResponseMapper responseMapper;

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable("id") Long id) throws ObjectNotFoundException {
        TaskDto task = taskService.getTaskById(id);
        return new ApiResponse<>(
                responseMapper.taskToResponse(task),
                HttpStatus.OK
        );
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<ListTaskResponse> getTasks(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) throws ObjectNotFoundException {
        List<TaskDto> tasks = taskService.getTasksByUser(userId, limit, offset);
        Long total = taskService.getTaskCountByUser(userId);
        return new ApiResponse<>(
                responseMapper.listTaskToResponse(tasks, total, limit, offset),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) throws ObjectNotFoundException {
        TaskDto task = taskService.createTask(request);
        return new ApiResponse<>(
                responseMapper.taskToResponse(task),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/start/{id}")
    public ApiResponse<TaskResponse> startTask(@PathVariable("id") Long id, @RequestBody Long userId) throws AccessRightsException, ObjectNotFoundException {
        TaskDto task = taskService.startTask(id, userId);
        return new ApiResponse<>(
                responseMapper.taskToResponse(task),
                HttpStatus.OK
        );
    }

    @PostMapping("/end/{id}")
    public ApiResponse<TaskResponse> endTask(@PathVariable("id") Long id, @RequestBody Long userId) throws AccessRightsException, ObjectNotFoundException {
        TaskDto task = taskService.endTask(id, userId);
        return new ApiResponse<>(
                responseMapper.taskToResponse(task),
                HttpStatus.OK
        );
    }
}
