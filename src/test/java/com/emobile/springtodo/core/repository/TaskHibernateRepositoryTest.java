package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.TaskStatus;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Sql(scripts = {"classpath:db/clear.sql", "classpath:db/init-user.sql", "classpath:db/init-task.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@Rollback
public class TaskHibernateRepositoryTest extends TestPostgresContainerConfig {

    @Autowired
    private TaskHibernateRepository taskRepository;

    @Test
    @DisplayName("Поиск задачи по ID - успешный сценарий")
    void findTaskById_Success() {
        Optional<Task> task = taskRepository.findById(1L);

        assertTrue(task.isPresent());
        assertEquals(1L, task.get().getId());
        assertEquals(1L, task.get().getUser().getId());
        assertEquals("Test Task 1", task.get().getTitle());
        assertEquals("Description 1", task.get().getDescription());
        assertEquals(TaskStatus.TODO, task.get().getStatus());
        assertEquals(Timestamp.valueOf("2025-07-11 12:00:00"), task.get().getDeadline());
        assertEquals(Timestamp.valueOf("2025-07-10 12:00:00"), task.get().getCreatedAt());
        assertNull(task.get().getEndDate());
    }

    @Test
    @DisplayName("Поиск задачи по ID - задача не найдена")
    void findTaskById_NotFound() {
        Optional<Task> task = taskRepository.findById(999L);

        assertFalse(task.isPresent());
    }

    @Test
    @DisplayName("Поиск задач по пользователю - успешный сценарий")
    void findTasksByUser_Success() {
        List<Task> tasks = taskRepository.findByUser(1L, 10, 0);

        assertEquals(2, tasks.size());
        assertEquals(1L, tasks.get(0).getId());
        assertEquals("Test Task 1", tasks.get(0).getTitle());
        assertEquals(TaskStatus.TODO, tasks.get(0).getStatus());
        assertEquals(2L, tasks.get(1).getId());
        assertEquals("Test Task 2", tasks.get(1).getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, tasks.get(1).getStatus());
    }

    @Test
    @DisplayName("Поиск задач по пользователю - задачи не найдены")
    @Sql(scripts = {"classpath:db/clear.sql"})
    void findTasksByUser_NotFound() {
        List<Task> tasks = taskRepository.findByUser(1L, 10, 0);

        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("Подсчет задач пользователя - успешный сценарий")
    void getTaskCountByUser_Success() {
        Long count = taskRepository.getTaskCountByUser(1L);

        assertEquals(2L, count);
    }

    @Test
    @DisplayName("Подсчет задач пользователя - задачи не найдены")
    @Sql(scripts = {"classpath:db/clear.sql"})
    void getTaskCountByUser_NotFound() {
        Long count = taskRepository.getTaskCountByUser(1L);

        assertEquals(0L, count);
    }

    @Test
    @DisplayName("Сохранение задачи - вставка новой задачи")
    void saveTask_Insert_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("'testuser1'");
        user.setCreatedAt(Timestamp.valueOf("2025-07-10 12:00:00"));
        user.setEmail("test1@example.com");

        Task task = new Task();
        task.setUser(user);
        task.setTitle("New Task");
        task.setDescription("New Description");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(Timestamp.valueOf("2025-07-12 12:00:00"));
        task.setCreatedAt(Timestamp.valueOf("2025-07-10 12:00:00"));

        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getId());
        assertEquals("New Task", savedTask.getTitle());
        assertEquals("New Description", savedTask.getDescription());
        assertEquals(TaskStatus.TODO, savedTask.getStatus());

        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());
        assertTrue(retrievedTask.isPresent());
        assertEquals(savedTask.getId(), retrievedTask.get().getId());
        assertEquals("New Task", retrievedTask.get().getTitle());
    }

    @Test
    @DisplayName("Сохранение задачи - обновление существующей задачи")
    void saveTask_Update_Success() {
        Task task = taskRepository.findById(1L).get();
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDeadline(Timestamp.valueOf("2025-07-13 12:00:00"));

        Task updatedTask = taskRepository.save(task);

        assertEquals(1L, updatedTask.getId());
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());

        Optional<Task> retrievedTask = taskRepository.findById(1L);
        assertTrue(retrievedTask.isPresent());
        assertEquals("Updated Task", retrievedTask.get().getTitle());
        assertEquals("Updated Description", retrievedTask.get().getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedTask.get().getStatus());
    }
}
