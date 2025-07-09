package com.emobile.springtodo.api.controller;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.api.mapper.ResponseMapper;
import com.emobile.springtodo.api.output.user.UserResponse;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import com.emobile.springtodo.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
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
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) throws ObjectNotFoundException {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(
                responseMapper.userToResponse(user)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) throws UserAlreadyExistsException {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                responseMapper.userToResponse(user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
