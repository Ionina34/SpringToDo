package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.repository.cantainer.TestPostgresContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Sql(scripts = {"classpath:db/clear.sql", "classpath:db/init-user.sql", "classpath:db/init-task.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@Rollback
public class UserHibernateRepositoryTest  extends TestPostgresContainerConfig{

    @Autowired
    private UserHibernateRepository userRepository;

    @Test
    @DisplayName("Поиск пользователя по ID - успешный сценарий")
    void findUserById_Success() {
        Optional<User> user = userRepository.findById(1L);

        assertTrue(user.isPresent());
        assertEquals(1L, user.get().getId());
        assertEquals("testuser1", user.get().getUsername());
        assertEquals("test1@example.com", user.get().getEmail());
        assertEquals(Timestamp.valueOf("2025-07-10 12:00:00"), user.get().getCreatedAt());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не найден")
    void findUserById_NotFound() {
        Optional<User> user = userRepository.findById(999L);

        assertFalse(user.isPresent());
    }

    @Test
    @DisplayName("Поиск пользователя по имени пользователя - успешный сценарий")
    void findUserByUsername_Success() {
        Optional<User> user = userRepository.findByUsername("testuser1");

        assertTrue(user.isPresent());
        assertEquals(1L, user.get().getId());
        assertEquals("testuser1", user.get().getUsername());
        assertEquals("test1@example.com", user.get().getEmail());
    }

    @Test
    @DisplayName("Поиск пользователя по имени пользователя - пользователь не найден")
    void findUserByUsername_NotFound() {
        Optional<User> user = userRepository.findByUsername("nonexistent");

        assertFalse(user.isPresent());
    }

    @Test
    @DisplayName("Поиск пользователя по email - успешный сценарий")
    void findUserByEmail_Success() {
        Optional<User> user = userRepository.findByEmail("test1@example.com");

        assertTrue(user.isPresent());
        assertEquals(1L, user.get().getId());
        assertEquals("testuser1", user.get().getUsername());
        assertEquals("test1@example.com", user.get().getEmail());
    }

    @Test
    @DisplayName("Поиск пользователя по email - пользователь не найден")
    void findUserByEmail_NotFound() {
        Optional<User> user = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(user.isPresent());
    }

    @Test
    @DisplayName("Сохранение пользователя - успешный сценарий")
    void saveUser_Success() {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setCreatedAt(Timestamp.valueOf("2025-07-10 12:00:00"));

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());

        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals(savedUser.getId(), retrievedUser.get().getId());
        assertEquals("newuser", retrievedUser.get().getUsername());
        assertEquals("new@example.com", retrievedUser.get().getEmail());
    }

    @Test
    @DisplayName("Удаление пользователя по ID - успешный сценарий")
    void deleteUserById_Success() {
        userRepository.deleteById(1L);

        Optional<User> user = userRepository.findById(1L);
        assertFalse(user.isPresent());
    }
}
