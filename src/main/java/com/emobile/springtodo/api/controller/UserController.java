package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
import com.emobile.springtodo.api.output.ApiResponse;
import com.emobile.springtodo.api.output.user.UserResponse;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import com.emobile.springtodo.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todo/user")
public class UserController {

    private final UserService userService;
    private final ResponseMapper responseMapper;

    @Autowired
    public UserController(UserService userService, ResponseMapper responseMapper) {
        this.userService = userService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") Long id) throws ObjectNotFoundException {
        UserDto user = userService.getUserDtoById(id);
        return new ApiResponse<>(
                responseMapper.userToResponse(user),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) throws UserAlreadyExistsException {
        UserDto user = userService.createUser(request);
        return new ApiResponse<>(
                responseMapper.userToResponse(user),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return new ApiResponse<>(
                Void.TYPE.cast(null),
                HttpStatus.OK
        );
    }
}
