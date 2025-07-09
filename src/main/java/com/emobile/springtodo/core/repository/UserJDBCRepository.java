package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.repository.mapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[]{id}),
                        new RowMapperResultSetExtractor<>(new UserRowMapper())
                )
        );
        return Optional.ofNullable(user);
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[]{username}),
                        new RowMapperResultSetExtractor<>(new UserRowMapper())
                )
        );
        return Optional.ofNullable(user);
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[]{email}),
                        new RowMapperResultSetExtractor<>(new UserRowMapper())
                )
        );
        return Optional.ofNullable(user);
    }

    public User save(User user) {
        user.setId(System.currentTimeMillis());
        String sql = "INSERT INTO users (id, username, email, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
        return user;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
