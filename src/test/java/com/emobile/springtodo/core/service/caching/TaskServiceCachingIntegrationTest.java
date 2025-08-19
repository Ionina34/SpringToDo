package com.emobile.springtodo.core.service.caching;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.core.config.properties.AppCacheProperties;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.mapper.TaskMapper;
import com.emobile.springtodo.core.repository.TaskHibernateRepository;
import com.emobile.springtodo.core.repository.cantainer.RedisContainer4Test;
import com.emobile.springtodo.core.repository.cantainer.TestPostgresContainerConfig;
import com.emobile.springtodo.core.service.TaskService;
import com.emobile.springtodo.core.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.metrics.task.TaskExecutorMetricsAutoConfiguration"
        }
)
@Transactional
@ContextConfiguration(classes = {TestPostgresContainerConfig.class})
@Sql(scripts = {"classpath:db/clear.sql", "classpath:db/init-user.sql", "classpath:db/init-task.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TaskServiceCachingIntegrationTest extends RedisContainer4Test {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private TaskHibernateRepository taskRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Получение задачи по ID - кэш заполняется")
    void whenGetTaskById_thenPopulateCache() {
        Long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task 1");
        taskDto.setDescription("Description 1");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым изначально");

        TaskDto result = taskService.getTaskById(taskId);
        System.out.println(result);

        assertEquals(taskDto, result, "Возвращенная задача должна соответствовать ожидаемой");
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи после вызова");
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASK_BY_ID + "::" + taskId), "Кэш должен содержать задачу по ID");
    }

    @Test
    @DisplayName("Получение задачи по ID - кэш-хит")
    void whenGetTaskByIdCacheHit_thenReturnCachedTask() {
        Long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(taskId);
        taskDto.setUserId(1L);
        taskDto.setTitle("Test Task 1");
        taskDto.setDescription("Description 1");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASK_BY_ID + "::" + taskId, taskDto);
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        TaskDto result = taskService.getTaskById(taskId);

        assertEquals(taskDto, result, "Возвращенная задача должна соответствовать кэшированной");
    }

    @Test
    @DisplayName("Получение задач пользователя - кэш заполняется")
    void whenGetTasksByUser_thenPopulateCache() {
        Long userId = 1L;
        int limit = 10;
        int offset = 0;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task 1");
        taskDto.setDescription("Description 1");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        TaskDto taskDto2 = new TaskDto();
        taskDto2.setId(2L);
        taskDto2.setUserId(userId);
        taskDto2.setTitle("Test Task 2");
        taskDto2.setDescription("Description 2");
        taskDto2.setStatus(TaskStatus.IN_PROGRESS);
        taskDto2.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto2.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым изначально");

        List<TaskDto> result = taskService.getTasksByUser(userId, limit, offset);

        System.out.println(result.toString());
        System.out.println(List.of(taskDto, taskDto2));

        assertEquals(List.of(taskDto, taskDto2), result, "Возвращенные задачи должны соответствовать ожидаемым");
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи после вызова");
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASKS_BY_USER + "::" + userId + "," + limit + "," + offset),
                "Кэш должен содержать задачи пользователя");
    }

    @Test
    @DisplayName("Получение задач пользователя - кэш-хит")
    void whenGetTasksByUserCacheHit_thenReturnCachedTasks() {
        Long userId = 1L;
        int limit = 10;
        int offset = 0;
        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setUserId(userId);
        taskDto.setTitle("Test Task 1");
        taskDto.setDescription("Description 1");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        TaskDto taskDto2 = new TaskDto();
        taskDto2.setId(2L);
        taskDto2.setUserId(userId);
        taskDto2.setTitle("Test Task 2");
        taskDto2.setDescription("Description 2");
        taskDto2.setStatus(TaskStatus.IN_PROGRESS);
        taskDto2.setDeadline(Timestamp.valueOf("2025-07-11 12:00:00"));
        taskDto2.setStart(Timestamp.valueOf("2025-07-10 12:00:00"));

        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASKS_BY_USER + userId + "," + limit + "," + offset, List.of(taskDto, taskDto2));
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        List<TaskDto> result = taskService.getTasksByUser(userId, limit, offset);

        assertEquals(List.of(taskDto, taskDto2), result, "Возвращенные задачи должны соответствовать кэшированным");
    }

    @Test
    @DisplayName("Получение количества задач пользователя - кэш заполняется")
    void whenGetTaskCountByUser_thenPopulateCache() {
        Long userId = 1L;

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым изначально");

        Long result = taskService.getTaskCountByUser(userId);

        assertEquals(2L, result, "Возвращенное количество задач должно быть 2");
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи после вызова");
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.TASK_COUNT + "::" + userId),
                "Кэш должен содержать количество задач пользователя");
    }

    @Test
    @DisplayName("Получение количества задач пользователя - кэш-хит")
    void whenGetTaskCountByUserCacheHit_thenReturnCachedCount() {
        Long userId = 1L;
        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASK_COUNT + "::" + userId, 2L);
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        Long result = taskService.getTaskCountByUser(userId);

        assertEquals(2L, result, "Возвращенное количество задач должно соответствовать кэшированному");
    }

    @Test
    @DisplayName("Создание задачи - очищает кэш")
    void whenCreateTask_thenEvictCache() {
        Long userId = 1L;
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(userId);
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setDeadline(Timestamp.valueOf("2025-07-12 12:00:00"));

        User user = new User();
        user.setId(userId);

        TaskDto taskDto = new TaskDto();
        taskDto.setId(3L);
        taskDto.setUserId(userId);
        taskDto.setTitle("New Task");
        taskDto.setDescription("New Description");
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setDeadline(Timestamp.valueOf("2025-07-12 12:00:00"));
        taskDto.setStart(null);

        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASKS_BY_USER + "::" + userId + ",10,0", List.of(new TaskDto()));
        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASK_COUNT + "::" + userId, 2L);
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        when(userService.findUserById(userId)).thenReturn(user);

        TaskDto result = taskService.createTask(request);
        result.setStart(null);

        assertEquals(taskDto, result, "Возвращенная задача должна соответствовать ожидаемой");
        assertNull(redisTemplate.opsForValue().get("tasks_user::" + userId + ",10,0"),
                "Кэш задач пользователя должен быть очищен");
        assertNull(redisTemplate.opsForValue().get("task_count::" + userId),
                "Кэш количества задач должен быть очищен");
    }
}
