package com.emobile.springtodo.core.service;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.core.config.properties.AppCacheProperties;
import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.mapper.TaskMapper;
import com.emobile.springtodo.core.repository.TaskJDBCRepository;
import com.emobile.springtodo.core.service.aspect.annotation.RightsVerification;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheManager = "redisCacheManager")
public class TaskService {

    private final TaskJDBCRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final MeterRegistry meterRegistry;

    private Task findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Task with id: " + id + " not found", Timestamp.from(Instant.now())));
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.TASK_BY_ID, key = "#id")
    public TaskDto getTaskById(Long id) throws ObjectNotFoundException {
        Task task = findTaskById(id);
        return taskMapper.taskToDto(task);
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.TASKS_BY_USER, key = "#userId + ',' + #limit + ',' + #offset")
    public List<TaskDto> getTasksByUser(Long userId, int limit, int offset) throws ObjectNotFoundException {
        List<Task> tasks = taskRepository.findByUser(userId, limit, offset);
        return taskMapper.listTaskToListTaskDto(tasks);
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.TASK_COUNT, key = "#userId")
    public Long getTaskCountByUser(Long userId) {
        return taskRepository.getTaskCountByUser(userId);
    }

    @Caching(
            evict = {
                    @CacheEvict(cacheNames = AppCacheProperties.CacheNames.TASKS_BY_USER, key = "#request.userId"),
                    @CacheEvict(cacheNames = AppCacheProperties.CacheNames.TASK_COUNT, key = "#request.userId")
            }
    )
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
                taskRepository.save(task)
        );
    }

    @RightsVerification
    public TaskDto endTask(Long id, Long userId) throws ObjectNotFoundException {
        Task task = findTaskById(id);
        task.setStatus(TaskStatus.DONE);
        task.setEndData(new Timestamp(new Date().getTime()));
        meterRegistry.counter("tasks.completed.total").increment();
        return taskMapper.taskToDto(
                taskRepository.save(task)
        );
    }
}
