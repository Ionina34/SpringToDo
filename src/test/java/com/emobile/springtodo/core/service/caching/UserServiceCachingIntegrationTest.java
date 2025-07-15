package com.emobile.springtodo.core.service.caching;


import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.core.config.properties.AppCacheProperties;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.mapper.UserMapper;
import com.emobile.springtodo.core.repository.UserJDBCRepository;
import com.emobile.springtodo.core.repository.cantainer.RedisContainer4Test;
import com.emobile.springtodo.core.repository.cantainer.TestPostgresContainerConfig;
import com.emobile.springtodo.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ContextConfiguration(classes = {TestPostgresContainerConfig.class})
@Sql(scripts = {"classpath:db/clear.sql", "classpath:db/init-user.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceCachingIntegrationTest extends RedisContainer4Test {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserJDBCRepository userRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Получение пользователя по ID - кэш заполняется")
    void whenGetUserById_thenPopulateCache() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser1");
        user.setEmail("test1@example.com");
        user.setCreatedAt(Timestamp.valueOf("2025-07-10 12:00:00"));

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testuser1");
        userDto.setEmail("test1@example.com");

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым изначально");

        UserDto result = userService.getUserDtoById(userId);

        assertEquals(userDto, result, "Возвращенный пользователь должен соответствовать ожидаемому");
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи после вызова");
        assertNotNull(redisTemplate.opsForValue().get(AppCacheProperties.CacheNames.USER_BY_ID + "::" + userId),
                "Кэш должен содержать пользователя по ID");
    }

    @Test
    @DisplayName("Получение пользователя по ID - кэш-хит")
    void whenGetUserByIdCacheHit_thenReturnCachedUser() {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testuser1");
        userDto.setEmail("test1@example.com");

        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.TASKS_BY_USER + "::" + userId, userDto);
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        UserDto result = userService.getUserDtoById(userId);

        assertEquals(userDto, result, "Возвращенный пользователь должен соответствовать кэшированному");
    }

    @Test
    @DisplayName("Создание пользователя - кэш не заполняется")
    void whenCreateUser_thenNoCachePopulation() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");

        User user = new User();
        user.setId(3L);
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setCreatedAt(Timestamp.valueOf("2025-07-10 12:00:00"));

        UserDto userDto = new UserDto();
        userDto.setId(3L);
        userDto.setUsername("newuser");
        userDto.setEmail("new@example.com");

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым изначально");

        UserDto result = userService.createUser(request);

        assertEquals(userDto, result, "Возвращенный пользователь должен соответствовать ожидаемому");
        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш не должен содержать записи после создания");
    }

    @Test
    @DisplayName("Удаление пользователя - очищает кэш")
    void whenDeleteUser_thenEvictCache() {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testuser1");
        userDto.setEmail("test1@example.com");

        redisTemplate.opsForValue().set(AppCacheProperties.CacheNames.USER_BY_ID + "::" + userId, userDto);
        assertFalse(redisTemplate.keys("*").isEmpty(), "Кэш должен содержать записи");

        userService.deleteUser(userId);

        assertTrue(redisTemplate.keys("*").isEmpty(), "Кэш должен быть пустым после удаления");
    }
}
