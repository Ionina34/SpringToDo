package com.emobile.springtodo.core.service;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.UserDto;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import com.emobile.springtodo.core.mapper.UserMapper;
import com.emobile.springtodo.core.repository.UserJDBCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class UserService {

    private final UserJDBCRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserJDBCRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) throws ObjectNotFoundException {
        User user = findUserById(id);
        return userMapper.userToDto(user);
    }

    protected User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("User with id: " + id + " not found"));
    }

    public UserDto createUser(CreateUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(v -> {
            throw new UserAlreadyExistsException("User with username: " + request.getUsername() + " already exists. Try a different name.");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(v -> {
            throw new UserAlreadyExistsException("User with email: " + request.getEmail() + " already exists. Try a different email.");
        });

        User user = userMapper.requestToUser(request);
        user.setCreatedAt(new Timestamp(new Date().getTime()));

        return userMapper.userToDto(
                userRepository.save(user)
        );
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
