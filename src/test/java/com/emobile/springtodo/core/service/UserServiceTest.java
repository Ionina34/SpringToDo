package com.emobile.springtodo.core.service;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import com.emobile.springtodo.core.mapper.UserMapper;
import com.emobile.springtodo.core.repository.UserHibernateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.cache.annotation.EnableCaching;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableCaching
public class UserServiceTest {

    @Mock
    private UserHibernateRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Timestamp timestamp;

    @BeforeEach
    void setUp() throws ParseException {
        objectMapper = new ObjectMapper();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = new Timestamp(sdf.parse("2026-03-15 00:00:00.0").getTime());
    }


    @Test
    @DisplayName("Получение пользователя по ID - успешный сценарий")
    void getUserById_Success() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCreatedAt(timestamp);

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.userToDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserDtoById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        JSONAssert.assertEquals(
                "{\"id\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(userRepository).findById(userId);
        verify(userMapper).userToDto(user);
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    void getUserById_NotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.getUserDtoById(userId));
        assertEquals("User with id: 1 not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успешный сценарий")
    void findUserById_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCreatedAt(timestamp);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не найден")
    void findUserById_NotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.findUserById(userId));
        assertEquals("User with id: 1 not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCreatedAt(timestamp);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userMapper.requestToUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        JSONAssert.assertEquals(
                "{\"id\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}",
                objectMapper.writeValueAsString(result),
                false
        );
        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
        verify(userMapper).requestToUser(request);
        verify(userRepository).save(user);
        verify(userMapper).userToDto(user);
    }

    @Test
    @DisplayName("Создание пользователя - пользователь с таким именем уже существует")
    void createUser_UsernameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request));
        assertEquals("User with username: testuser already exists. Try a different name.", exception.getMessage());
        verify(userRepository).findByUsername(request.getUsername());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Создание пользователя - пользователь с таким email уже существует")
    void createUser_EmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request));
        assertEquals("User with email: test@example.com already exists. Try a different email.", exception.getMessage());
        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void deleteUser_Success() {
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
}
