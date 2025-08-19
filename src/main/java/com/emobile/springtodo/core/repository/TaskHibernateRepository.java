package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHibernateRepository extends JpaRepository<Task, Long> {

    @Query(value = "SELECT * FROM tasks WHERE user_id = :userId ORDER BY id LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Task> findByUser(@Param("userId") Long userId,
                          @Param("limit") int limit,
                          @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE user_id = :userId",
            nativeQuery = true)
    Long getTaskCountByUser(@Param("userId") Long userId);
}
