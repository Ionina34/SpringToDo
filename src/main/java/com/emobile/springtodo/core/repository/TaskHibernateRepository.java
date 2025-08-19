package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.Task;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class TaskHibernateRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public TaskHibernateRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<Task> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Task.class, id));
        }
    }

    public List<Task> findByUser(Long userId, int limit, int offset) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Task t JOIN t.user u WHERE u.id = :userId";
            return session.createQuery(hql, Task.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .setFirstResult(offset)
                    .getResultList();
        }
    }

    public Long getTaskCountByUser(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId";
            return session.createQuery(hql, Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        }
    }

    public Task save(Task task) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (task.getId() == null) {
                task.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                session.persist(task);
            } else {
                session.merge(task);
            }
            session.getTransaction().commit();
            return task;
        }
    }
}
