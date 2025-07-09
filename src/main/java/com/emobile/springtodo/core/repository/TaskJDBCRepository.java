package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.repository.mapper.TaskRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        Task task = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[]{id}),
                        new RowMapperResultSetExtractor<>(new TaskRowMapper(), 1)
                )
        );
        return Optional.ofNullable(task);
    }

    public List<Task> findByUser(Long userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        return jdbcTemplate.query(
                sql,
                new ArgumentPreparedStatementSetter(new Object[]{userId}),
                new TaskRowMapper()
        );
    }

    public Task save(Task task) {
        task.setId(System.currentTimeMillis());
        task.setCreatedAt(new Timestamp(new Date().getTime()));
        String sql = "SELECT INTO tasks (id, user_id, title, description, status, deadline, created_at, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                task.getId(),
                task.getUserId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getEndData()
        );
        return task;
    }

    public Task update(Task task) {
        Task existsTask = findById(task.getId()).orElse(null);

        if (existsTask != null) {
            String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, deadline = ?, created_at = ?, end_data = ? WHERE id = ?";
            jdbcTemplate.update(
                    sql,
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus().toString(),
                    task.getDeadline(),
                    task.getCreatedAt(),
                    task.getEndData(),
                    task.getId()
            );
            return task;
        }
        throw new ObjectNotFoundException("Task with id: " + task.getId() + " not found");
    }
}
