package com.emobile.springtodo.core.repository;

import com.emobile.springtodo.core.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserHibernateRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserHibernateRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM User u WHERE u.username = :username";
            return Optional.ofNullable(
                    session.createQuery(hql, User.class)
                            .setParameter("username", username)
                            .uniqueResult()
            );
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM User u WHERE u.email = :email";
            return Optional.ofNullable(
                    session.createQuery(hql, User.class)
                            .setParameter("email", email)
                            .uniqueResult()
            );
        }
    }

    public User save(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            session.persist(user);
            session.getTransaction().commit();
            return user;
        }
    }

    public void deleteById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            session.getTransaction().commit();
        }
    }
}
