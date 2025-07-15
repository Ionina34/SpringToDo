package com.emobile.springtodo;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.core.config.properties.AppCacheProperties;
import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.repository.TaskJDBCRepository;
import com.emobile.springtodo.core.repository.UserJDBCRepository;
import com.emobile.springtodo.core.repository.cantainer.RedisContainer4Test;
import com.emobile.springtodo.core.repository.cantainer.TestPostgresContainerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(classes = TestPostgresContainerConfig.class)
@Transactional
@Sql(scripts = {"classpath:db/clear.sql", "classpath:db/init-user.sql", "classpath:db/init-task.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SpringToDoApplicationTests extends RedisContainer4Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TaskJDBCRepository taskRepository;

    @Autowired
    private UserJDBCRepository userRepository;

    @BeforeEach
    void before() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Проверка загрузки контекста приложения")
    void contextLoads() {
    }

    @Test
    @DisplayName("Получение задачи по ID - успешный сценарий")
    void whenGetTaskById_thenReturnTask() throws Exception {
        String expectedJson = """
                {
                    "data": {
                        "id": 1,
                        "userId": 1,
                        "title": "Test Task 1",
                        "description": "Description 1",
                        "status": "TODO",
                        "start": "2025-07-10 07:00:00.0",
                        "deadline": "2025-07-11 07:00:00.0",
                        "end": null
                        },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASK_BY_ID + "::1"), "Кэш должен содержать задачу по ID");
        assertTrue(taskRepository.findById(1L).isPresent(), "Задача должна существовать в БД");
    }

    @Test
    @DisplayName("Получение задачи по ID - задача не найдена")
    void whenGetTaskByIdNotFound_thenReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/todo/task/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение задач пользователя - успешный сценарий")
    void whenGetTasksByUser_thenReturnTasks() throws Exception {
        String expectedJson = """
                {
                    "data": {
                        "tasks": [
                            {
                                "id": 1,
                                "userId": 1,
                                "title": "Test Task 1",
                                "description": "Description 1",
                                "status": "TODO",
                                "start": "2025-07-10 07:00:00.0",
                                "deadline": "2025-07-11 07:00:00.0",
                                "end": null
                            },
                            {
                                "id": 2,
                                "userId": 1,
                                "title": "Test Task 2",
                                "description": "Description 2",
                                "status": "IN_PROGRESS",
                                "start": "2025-07-10 07:00:00.0",
                                "deadline": "2025-07-11 07:00:00.0",
                                "end": null
                            }
                        ],
                        "total": 2,
                        "limit": 10,
                        "offset": 0
                    },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/user/1")
                        .param("userId", "1")
                        .param("limit", "10")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASKS_BY_USER + "::1,10,0"), "Кэш должен содержать задачи пользователя");
        assertEquals(2, taskRepository.findByUser(1L, 10, 0).size(), "В БД должно быть 2 задачи для пользователя");
    }

    @Test
    @DisplayName("Создание задачи - успешный сценарий")
    void whenCreateTask_thenReturnCreatedTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(1L);
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setDeadline(Timestamp.valueOf("2025-07-14 07:00:00.0"));

        String requestJson = objectMapper.writeValueAsString(request);
        String expectedJson = """
                    {
                        "data": {
                            "id": 3,
                            "userId": 1,
                            "title": "New Task",
                            "description": "New Description",
                            "status": "TODO",
                            "start": "null",
                            "deadline": "2025-07-14 07:00:00.0",
                            "end": null
                            },
                        "status": "OK"
                    }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        assertNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASKS_BY_USER + "::1,10,0"), "Кэш задач пользователя должен быть очищен");
        assertNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASK_BY_ID + "::1"), "Кэш количества задач должен быть очищен");
        assertTrue(taskRepository.findById(1L).isPresent(), "Задача должна быть создана в БД");
    }

    @Test
    @DisplayName("Начало задачи - успешный сценарий")
    void whenStartTask_thenReturnUpdatedTask() throws Exception {
        String expectedJson = """
                {
                    "data": {
                        "id": 1,
                        "userId": 1,
                        "title": "Test Task 1",
                        "description": "Description 1",
                        "status": "IN_PROGRESS",
                        "start": "2025-07-10 07:00:00.0",
                        "deadline": "2025-07-11 07:00:00.0",
                        "end": null
                        },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/start/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        Task task = taskRepository.findById(1L).orElseThrow();
        assertEquals("IN_PROGRESS", task.getStatus().name(), "Статус задачи в БД должен быть IN_PROGRESS");
    }

    @Test
    @DisplayName("Завершение задачи - успешный сценарий")
    void whenEndTask_thenReturnUpdatedTask() throws Exception {
        String expectedJson = """
                {
                    "data": {
                        "id": 2,
                        "userId": 1,
                        "title": "Test Task 2",
                        "description": "Description 2",
                        "status": "DONE",
                        "start": "2025-07-10 07:00:00.0",
                        "deadline": "2025-07-11 07:00:00.0",
                        "end": now
                        },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/todo/task/end/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        Task task = taskRepository.findById(2L).orElseThrow();
        assertEquals("DONE", task.getStatus().name(), "Статус задачи в БД должен быть DONE");
        assertNotNull(task.getEndData(), "Дата завершения в БД должна быть установлена");
    }

    @Test
    @DisplayName("Получение пользователя по ID - успешный сценарий")
    void whenGetUserById_thenReturnUser() throws Exception {
        String expectedJson = """
                {
                    "data": {
                        "id": 1,
                        "username": "testuser1",
                        "email": "test1@example.com"
                    },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(get("/api/v1/todo/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.USER_BY_ID + "::1"), "Кэш должен содержать пользователя по ID");
        assertTrue(userRepository.findById(1L).isPresent(), "Пользователь должен существовать в БД");
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    void whenGetUserByIdNotFound_thenReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/todo/user/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void whenCreateUser_thenReturnCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");

        String requestJson = objectMapper.writeValueAsString(request);
        String expectedJson = """
                {
                    "data": {
                        "id": 3,
                        "username": "newuser",
                        "email": "new@example.com"
                    }
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/todo/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш не должен содержать записи после создания");
        assertTrue(userRepository.findByUsername("newuser").isPresent(), "Пользователь должен быть создан в БД");
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void whenDeleteUser_thenReturnNoContent() throws Exception {
        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.USER_BY_ID + "::1", new CreateUserRequest());
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        mockMvc.perform(delete("/api/v1/todo/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым после удаления");
        assertFalse(userRepository.findById(1L).isPresent(), "Пользователь должен быть удален из БД");
    }

    @Test
    @DisplayName("Получение задач из кэша - успешный сценарий")
    void whenGetTasksByUserFromCache_thenReturnCachedTasks() throws Exception {
        // Сначала выполняем запрос, чтобы заполнить кэш
        mockMvc.perform(get("/api/v1/todo/task/user/1")
                        .param("limit", "10")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Проверяем, что кэш заполнен
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASKS_BY_USER + "::1,10,0"), "Кэш должен содержать задачи пользователя");

        // Повторный запрос должен использовать кэш
        String expectedJson = """
                {
                    "data": {
                        "tasks": [
                            {
                                "id": 1,
                                "userId": 1,
                                "title": "Test Task 1",
                                "description": "Description 1",
                                "status": "TODO",
                                "start": "2025-07-10 07:00:00.0",
                                "deadline": "2025-07-11 07:00:00.0",
                                "end": null
                            },
                            {
                                "id": 2,
                                "userId": 1,
                                "title": "Test Task 2",
                                "description": "Description 2",
                                "status": "IN_PROGRESS",
                                "start": "2025-07-10 07:00:00.0",
                                "deadline": "2025-07-11 07:00:00.0",
                                "end": null
                            }
                        ],
                        "total": 2,
                        "limit": 10,
                        "offset": 0
                    },
                    "status": "OK"
                }
                """;

        MvcResult result = mockMvc.perform(get("/api/v1/todo/task/user/1")
                        .param("limit", "10")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }
}
