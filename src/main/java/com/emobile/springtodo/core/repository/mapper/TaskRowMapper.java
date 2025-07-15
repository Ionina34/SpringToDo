package com.emobile.springtodo.core.repository.mapper;

import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.TaskStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskRowMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(TaskStatus.valueOf(
                rs.getString("status"))
        );
        task.setDeadline(rs.getTimestamp("deadline"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setEndData(rs.getTimestamp("end_date"));
        return task;
    }
}
