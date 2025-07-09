package com.emobile.springtodo.core.service;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.mapper.TaskMapper;
import com.emobile.springtodo.core.repository.TaskJDBCRepository;
import com.emobile.springtodo.core.service.aspect.annotation.RightsVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class TaskService {

    private final TaskJDBCRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(TaskJDBCRepository taskRepository, UserService userService, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.taskMapper = taskMapper;
    }

    @Cacheable(value="tasks", key="#id")
    private Task findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Task with id: " + id + " not found"));
    }

    public TaskDto getTaskById(Long id) throws ObjectNotFoundException {
        Task task = findTaskById(id);
        return taskMapper.taskToDto(task);
    }

    @Cacheable(value="tasks_user", key="#userId")
    public List<TaskDto> getTasksByUser(Long userId) throws ObjectNotFoundException {
        List<Task> tasks = taskRepository.findByUser(userId);
        return taskMapper.listTaskToListTaskDto(tasks);
    }

    public TaskDto createTask(CreateTaskRequest request) throws ObjectNotFoundException {
        User user = userService.findUserById(request.getUserId());

        Task task = taskMapper.requestToTask(request);
        task.setUserId(user.getId());
        task.setStatus(TaskStatus.TODO);
        return taskMapper.taskToDto(
                taskRepository.save(task)
        );
    }

    @RightsVerification
    public TaskDto startTask(Long id, Long userId) throws ObjectNotFoundException {
        Task task = findTaskById(id);
        task.setStatus(TaskStatus.IN_PROGRESS);
        return taskMapper.taskToDto(
                taskRepository.update(task)
        );
    }

    @RightsVerification
    public TaskDto endTask(Long id, Long userId) throws ObjectNotFoundException {
        Task task = findTaskById(id);
        task.setStatus(TaskStatus.DONE);
        task.setEndData(new Timestamp(new Date().getTime()));
        return taskMapper.taskToDto(
                taskRepository.update(task)
        );
    }
}
