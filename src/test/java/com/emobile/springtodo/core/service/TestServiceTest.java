package com.emobile.springtodo.core.service;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.mapper.TaskMapper;
import com.emobile.springtodo.core.repository.TaskJDBCRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableCaching
public class TestServiceTest {

    @Mock
    private TaskJDBCRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskService taskService;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Timestamp timestamp;

    @BeforeEach
    void setUp() throws ParseException {
        objectMapper = new ObjectMapper();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = new Timestamp(sdf.parse("2026-03-15 00:00:00.0").getTime());
    }

    @Test
    @DisplayName("Получение задачи по ID - успешный сценарий")
    void getTaskById_Success() throws Exception {
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(timestamp);
        task.setCreatedAt(timestamp);
        task.setEndData(timestamp);

        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(task.getDeadline());
        taskDto.setStart(task.getCreatedAt());
        taskDto.setEnd(task.getEndData());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.taskToDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        JSONAssert.assertEquals(
                "{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(taskRepository).findById(taskId);
        verify(taskMapper).taskToDto(task);
    }

    @Test
    @DisplayName("Получение задачи по ID - задача не найдена")
    void getTaskById_NotFound() {
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> taskService.getTaskById(taskId));
        assertEquals("Task with id: 1 not found", exception.getMessage());
        verify(taskRepository).findById(taskId);
    }

    @Test
    @DisplayName("Получение списка задач пользователя - успешный сценарий")
    void getTasksByUser_Success() throws Exception {
        Long userId = 1L;
        Long taskId =1L;
        int limit = 10;
        int offset = 0;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(timestamp);
        task.setCreatedAt(timestamp);
        task.setEndData(timestamp);

        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(task.getDeadline());
        taskDto.setStart(task.getCreatedAt());
        taskDto.setEnd(task.getEndData());

        when(taskRepository.findByUser(userId, limit, offset)).thenReturn(List.of(task));
        when(taskMapper.listTaskToListTaskDto(List.of(task))).thenReturn(List.of(taskDto));

        List<TaskDto> result = taskService.getTasksByUser(userId, limit, offset);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getId());
        JSONAssert.assertEquals(
                "[{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\"}]",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(taskRepository).findByUser(userId, limit, offset);
        verify(taskMapper).listTaskToListTaskDto(List.of(task));
    }

    @Test
    @DisplayName("Получение количества задач пользователя - успешный сценарий")
    void getTaskCountByUser_Success() {
        Long userId = 1L;
        when(taskRepository.getTaskCountByUser(userId)).thenReturn(5L);

        Long result = taskService.getTaskCountByUser(userId);

        assertEquals(5L, result);
        verify(taskRepository).getTaskCountByUser(userId);
    }

    @Test
    @DisplayName("Создание задачи - успешный сценарий")
    void createTask_Success() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(1L);
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setDeadline(timestamp);

        User user = new User();
        user.setId(1L);

        Task task = new Task();
        task.setId(1L);
        task.setUserId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(request.getDeadline());
        task.setCreatedAt(timestamp);
        task.setEndData(request.getDeadline());

        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(task.getDeadline());
        taskDto.setStart(task.getCreatedAt());
        taskDto.setEnd(task.getEndData());

        when(userService.findUserById(request.getUserId())).thenReturn(user);
        when(taskMapper.requestToTask(request)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.taskToDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        JSONAssert.assertEquals(
                "{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(userService).findUserById(request.getUserId());
        verify(taskMapper).requestToTask(request);
        verify(taskRepository).save(task);
        verify(taskMapper).taskToDto(task);
    }

    @Test
    @DisplayName("Создание задачи - пользователь не найден")
    void createTask_UserNotFound() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(1L);
        when(userService.findUserById(request.getUserId())).thenThrow(new ObjectNotFoundException("User with id: 1 not found", timestamp));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> taskService.createTask(request));
        assertEquals("User with id: 1 not found", exception.getMessage());
        verify(userService).findUserById(request.getUserId());
        verifyNoInteractions(taskMapper, taskRepository);
    }

    @Test
    @DisplayName("Запуск задачи - успешный сценарий")
    void startTask_Success() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(timestamp);
        task.setCreatedAt(timestamp);
        task.setEndData(timestamp);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setUserId(userId);
        updatedTask.setTitle("Test Task");
        updatedTask.setDescription("Test Description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setDeadline(task.getDeadline());
        updatedTask.setCreatedAt(task.getCreatedAt());
        updatedTask.setEndData(task.getEndData());

        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.IN_PROGRESS);
        taskDto.setDeadline(task.getDeadline());
        taskDto.setStart(task.getCreatedAt());
        taskDto.setEnd(task.getEndData());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.taskToDto(updatedTask)).thenReturn(taskDto);

        TaskDto result = taskService.startTask(taskId, userId);

        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        JSONAssert.assertEquals(
                "{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"IN_PROGRESS\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).taskToDto(updatedTask);
    }

    @Test
    @DisplayName("Запуск задачи - задача не найдена")
    void startTask_NotFound() {
        Long taskId = 1L;
        Long userId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> taskService.startTask(taskId, userId));
        assertEquals("Task with id: 1 not found", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Завершение задачи - успешный сценарий")
    void endTask_Success() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDeadline(timestamp);
        task.setCreatedAt(timestamp);
        task.setEndData(timestamp);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setUserId(userId);
        updatedTask.setTitle("Test Task");
        updatedTask.setDescription("Test Description");
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setDeadline(task.getDeadline());
        updatedTask.setCreatedAt(task.getCreatedAt());
        updatedTask.setEndData(timestamp);

        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.DONE);
        taskDto.setDeadline(task.getDeadline());
        taskDto.setStart(task.getCreatedAt());
        taskDto.setEnd(updatedTask.getEndData());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.taskToDto(updatedTask)).thenReturn(taskDto);
        when(meterRegistry.counter("tasks.completed.total")).thenReturn(counter);

        TaskDto result = taskService.endTask(taskId, userId);

        assertNotNull(result);
        assertEquals(TaskStatus.DONE, result.getStatus());
        JSONAssert.assertEquals(
                "{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"DONE\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).taskToDto(updatedTask);
        verify(meterRegistry).counter("tasks.completed.total");
        verify(counter).increment();
    }

    @Test
    @DisplayName("Завершение задачи - задача не найдена")
    void endTask_NotFound() {
        Long taskId = 1L;
        Long userId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> taskService.endTask(taskId, userId));
        assertEquals("Task with id: 1 not found", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper, meterRegistry);
    }
}
