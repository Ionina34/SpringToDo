package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
import com.emobile.springtodo.api.output.user.UserResponse;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import com.emobile.springtodo.core.service.UserService;
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
import java.util.TimeZone;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@EnableWebMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
    @DisplayName("Получение пользователя по ID - успешный сценарий")
    void getUser_Success() throws Exception {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testuser");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setUsername("testuser");

        when(userService.getUserDtoById(userId)).thenReturn(userDto);
        when(responseMapper.userToResponse(userDto)).thenReturn(userResponse);

        MvcResult result = mockMvc.perform(get("/api/v1/todo/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String expectedJson = "{\"data\":{\"id\":1,\"username\":\"testuser\"},\"status\":\"OK\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    void getUser_NotFound() throws Exception {
        Long userId = 1L;
        when(userService.getUserDtoById(userId)).thenThrow(new ObjectNotFoundException("User not found with ID: " + userId, timestamp));

        MvcResult result = mockMvc.perform(get("/api/v1/todo/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String formatterTimestamp = sdf.format(timestamp);
        String expectedJson = "{\"data\":{\"status\":404,\"message\":\"User not found with ID: 1\",\"timestamp\":\"" + formatterTimestamp + "\"},\"status\":\"NOT_FOUND\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@mail.ru");

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@mail.ru");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@mail.ru");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);
        when(responseMapper.userToResponse(userDto)).thenReturn(userResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/todo/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedJson = "{\"data\":{\"id\":1,\"username\":\"testuser\",\"email\":\"test@mail.ru\"},\"status\":\"CREATED\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Создание пользователя - пользователь уже существует")
    void createUser_AlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@mail.ru");

        when(userService.createUser(any(CreateUserRequest.class))).thenThrow(new UserAlreadyExistsException("User already exists with username: testuser", timestamp));

        MvcResult result = mockMvc.perform(post("/api/v1/todo/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expectedJson = "{\"data\":{\"status\":400,\"message\":\"User already exists with username: testuser\",\"timestamp\":\"" + sdf.format(timestamp) + "\"},\"status\":\"BAD_REQUEST\"}";
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(), false);
    }

    @Test
    @DisplayName("Создание пользователя - неверные входные данные")
    void createUser_ValidationError() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        MvcResult result = mockMvc.perform(post("/api/v1/todo/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"data\":{\"status\":400,\"message\":\"Request is not valid\",\"timestamp\":\"" + responseContent.split("\"timestamp\":\"")[1].split("\"")[0] + "\",\"errors\":[{\"field\":\"email\",\"message\":\"Email address must be specified\"},{\"field\":\"username\",\"message\":\"Username must be specified\"}]},\"status\":\"BAD_REQUEST\"}", responseContent, false);
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void deleteUser_Success() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/todo/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}
