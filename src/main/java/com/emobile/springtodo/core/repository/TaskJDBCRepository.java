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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

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

    public List<Task> findByUser(Long userId, int limit, int offset) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(
                sql,
                new ArgumentPreparedStatementSetter(new Object[]{userId, limit, offset}),
                new TaskRowMapper()
        );
    }

    public Long getTaskCountByUser(Long userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        return DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[]{userId}),
                        new RowMapperResultSetExtractor<>(
                                (rs, rowNum) -> rs.getLong(1), 1)
                )
        );
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setCreatedAt(new Timestamp(new Date().getTime()));
            String sql = "INSERT INTO tasks (user_id, title, description, status, deadline, created_at, end_date) VALUES (?, ?, ?, ?::task_status, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
                stmt.setLong(1, task.getUserId());
                stmt.setString(2, task.getTitle());
                stmt.setString(3, task.getDescription());
                stmt.setString(4, task.getStatus().name());
                stmt.setTimestamp(5, task.getDeadline());
                stmt.setTimestamp(6, task.getCreatedAt());
                stmt.setTimestamp(7, task.getEndData());
                return stmt;
            }, keyHolder);
            task.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        } else {
            String sql = "UPDATE tasks SET title = ?, description = ?, status = ?::task_status, deadline = ?, created_at = ?, end_date = ? WHERE id = ?";
            jdbcTemplate.update(
                    sql,
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus().name(),
                    task.getDeadline(),
                    task.getCreatedAt(),
                    task.getEndData(),
                    task.getId()
            );
        }
        return task;
    }
}
