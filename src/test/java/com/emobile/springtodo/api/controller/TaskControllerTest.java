package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
import com.emobile.springtodo.api.output.task.ListTaskResponse;
import com.emobile.springtodo.api.output.task.TaskResponse;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.AccessRightsException;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@EnableWebMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ResponseMapper responseMapper;

    @Autowired
    private ObjectMapper objectMapper;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Timestamp timestamp;

    @BeforeEach
    void setUp() throws ParseException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = new Timestamp(sdf.parse("2026-03-15 00:00:00.0").getTime());
    }

    @Test
    @DisplayName("Получение задачи по ID - успешный сценарий")
    void getTask_Success() throws Exception {
        Long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(timestamp);
        taskDto.setStart(timestamp);
        taskDto.setEnd(timestamp);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(taskId);
        taskResponse.setUserId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.TODO);
        taskResponse.setDeadline(taskDto.getDeadline());
        taskResponse.setStart(timestamp);
        taskResponse.setEnd(timestamp);

        when(taskService.getTaskById(taskId)).thenReturn(taskDto);
        when(responseMapper.taskToResponse(taskDto)).thenReturn(taskResponse);

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\",\"start\":\"" + formattedTimestamp + "\",\"deadline\":\"" + formattedTimestamp + "\",\"end\":\"" + formattedTimestamp + "\"},\"status\":\"OK\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Получение задачи по ID - задача не найдена")
    void getTask_NotFound() throws Exception {
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenThrow(new ObjectNotFoundException("Task not found with ID: " + taskId, timestamp));

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"status\":404,\"message\":\"Task not found with ID: 1\",\"timestamp\":\"" + formattedTimestamp + "\"},\"status\":\"NOT_FOUND\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Получение списка задач пользователя - успешный сценарий")
    void getTasks_Success() throws Exception {
        Long userId = 1L;
        int limit = 10;
        int offset = 0;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(timestamp);
        taskDto.setStart(timestamp);
        taskDto.setEnd(timestamp);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setUserId(userId);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.TODO);
        taskResponse.setDeadline(taskDto.getDeadline());
        taskResponse.setStart(taskDto.getStart());
        taskResponse.setEnd(taskDto.getEnd());

        ListTaskResponse listTaskResponse = new ListTaskResponse(List.of(taskResponse), 1L, limit, offset);

        when(taskService.getTasksByUser(userId, limit, offset)).thenReturn(List.of(taskDto));
        when(taskService.getTaskCountByUser(userId)).thenReturn(1L);
        when(responseMapper.listTaskToResponse(eq(List.of(taskDto)), eq(1L), eq(limit), eq(offset))).thenReturn(listTaskResponse);

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/user/{userId}", userId)
                        .param("limit", String.valueOf(limit))
                        .param("offset", String.valueOf(offset))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"tasks\":[{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\",\"start\":\"" + formattedTimestamp + "\",\"deadline\":\"" + formattedTimestamp + "\",\"end\":\"" + formattedTimestamp + "\"}],\"total\":1,\"limit\":10,\"offset\":0},\"status\":\"OK\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Получение списка задач пользователя - пользователь не найдены")
    void getTasks_NotFound() throws Exception {
        Long userId = 1L;
        int limit = 10;
        int offset = 0;
        when(taskService.getTasksByUser(userId, limit, offset)).thenThrow(new ObjectNotFoundException("No user found for user with ID: " + userId, timestamp));

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/user/{userId}", userId)
                        .param("limit", String.valueOf(limit))
                        .param("offset", String.valueOf(offset))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"status\":404,\"message\":\"No user found for user with ID: 1\",\"timestamp\":\"" + formattedTimestamp + "\"}, \"status\":\"NOT_FOUND\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Создание задачи - успешный сценарий")
    void createTask_Success() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(1L);
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setDeadline(timestamp);

        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(request.getDeadline());
        taskDto.setStart(timestamp);
        taskDto.setEnd(timestamp);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setUserId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.TODO);
        taskResponse.setDeadline(taskDto.getDeadline());
        taskResponse.setStart(taskDto.getStart());
        taskResponse.setEnd(taskDto.getEnd());

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskDto);
        when(responseMapper.taskToResponse(taskDto)).thenReturn(taskResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"TODO\",\"start\":\"" + formattedTimestamp + "\",\"deadline\":\"" + formattedTimestamp + "\",\"end\":\"" + formattedTimestamp + "\"},\"status\":\"CREATED\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Запуск задачи - успешный сценарий")
    void startTask_Success() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.IN_PROGRESS);
        taskDto.setDeadline(timestamp);
        taskDto.setStart(timestamp);
        taskDto.setEnd(timestamp);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(taskId);
        taskResponse.setUserId(userId);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.IN_PROGRESS);
        taskResponse.setDeadline(taskDto.getDeadline());
        taskResponse.setStart(taskDto.getStart());
        taskResponse.setEnd(taskDto.getEnd());

        when(taskService.startTask(taskId, userId)).thenReturn(taskDto);
        when(responseMapper.taskToResponse(taskDto)).thenReturn(taskResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/start/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"IN_PROGRESS\",\"start\":\"" + formattedTimestamp + "\",\"deadline\":\"" + formattedTimestamp + "\",\"end\":\"" + formattedTimestamp + "\"},\"status\":\"OK\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Запуск задачи - доступ запрещен")
    void startTask_AccessDenied() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        when(taskService.startTask(taskId, userId)).thenThrow(new AccessRightsException("User ID " + userId + " does not have access to task ID " + taskId, timestamp));

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/start/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userId)))
                .andExpect(status().isForbidden())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"status\":403,\"message\":\"User ID 1 does not have access to task ID 1\",\"timestamp\":\"" + formattedTimestamp + "\"},\"status\":\"FORBIDDEN\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Создание задачи - неверные входные данные")
    void createTask_ValidationError() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"data\":{\"status\":400,\"message\":\"Request is not valid\",\"timestamp\":\"" + responseContent.split("\"timestamp\":\"")[1].split("\"")[0] + "\",\"errors\":[{\"field\":\"description\",\"message\":\"Description must be specified\"},{\"field\":\"userId\",\"message\":\"User ID must be specified\"},{\"field\":\"deadline\",\"message\":\"Deadline must be specified\"},{\"field\":\"title\",\"message\":\"Title must be specified\"}]},\"status\":\"BAD_REQUEST\"}", responseContent, false);
    }

    @Test
    @DisplayName("Завершение задачи - успешный сценарий")
    void endTask_Success() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.DONE);
        taskDto.setDeadline(timestamp);
        taskDto.setStart(timestamp);
        taskDto.setEnd(timestamp);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(taskId);
        taskResponse.setUserId(userId);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.DONE);
        taskResponse.setDeadline(taskDto.getDeadline());
        taskResponse.setStart(taskDto.getStart());
        taskResponse.setEnd(taskDto.getEnd());

        when(taskService.endTask(taskId, userId)).thenReturn(taskDto);
        when(responseMapper.taskToResponse(taskDto)).thenReturn(taskResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/end/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"id\":1,\"userId\":1,\"title\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"DONE\",\"start\":\"" + formattedTimestamp + "\",\"deadline\":\"" + formattedTimestamp + "\",\"end\":\"" + formattedTimestamp + "\"},\"status\":\"OK\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Завершение задачи - задача не найдена")
    void endTask_NotFound() throws Exception {
        Long taskId = 1L;
        Long userId = 1L;
        when(taskService.endTask(taskId, userId)).thenThrow(new ObjectNotFoundException("Task not found with ID: " + taskId, timestamp));

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/end/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userId)))
                .andExpect(status().isNotFound())
                .andReturn();

        String formattedTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"status\":404,\"message\":\"Task not found with ID: 1\",\"timestamp\":\"" + formattedTimestamp + "\"},\"status\":\"NOT_FOUND\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }
}
