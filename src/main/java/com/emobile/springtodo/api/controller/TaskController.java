package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
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
    public ResponseEntity<TaskResponse> getTask(@PathVariable("id") Long id) throws ObjectNotFoundException {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(
                responseMapper.taskToResponse(task)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ListTaskResponse> getTasks(@PathVariable("userId") Long userId) throws ObjectNotFoundException {
        List<TaskDto> tasks = taskService.getTasksByUser(userId);
        return ResponseEntity.ok(
                responseMapper.listTaskToResponse(tasks)
        );
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) throws ObjectNotFoundException {
        TaskDto task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                responseMapper.taskToResponse(task)
        );
    }

    @PostMapping("/start/{id}")
    public ResponseEntity<TaskResponse> startTask(@PathVariable("id") Long id, @RequestBody Long userId) throws AccessRightsException, ObjectNotFoundException {
        TaskDto task = taskService.startTask(id, userId);
        return ResponseEntity.ok(
                responseMapper.taskToResponse(task)
        );
    }

    @PostMapping("/end/{id}")
    public ResponseEntity<TaskResponse> endTask(@PathVariable("id") Long id, @RequestBody Long userId) throws AccessRightsException, ObjectNotFoundException {
        TaskDto task = taskService.endTask(id, userId);
        return ResponseEntity.ok(
                responseMapper.taskToResponse(task)
        );
    }
}
