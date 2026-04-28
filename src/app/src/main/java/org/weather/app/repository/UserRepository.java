package org.weather.app.repository;

import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.weather.app.model.User;

import java.util.Optional;

@Repository
@Transactional
public class UserRepository {
    private final SessionFactory sessionFactory;

    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(User user) {
        sessionFactory.getCurrentSession().persist(user);
    }

    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .createQuery("FROM User WHERE id = :id", User.class)
                        .setParameter("id", id)
                        .uniqueResult()
        );
    }

    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .createQuery("FROM User WHERE login = :login", User.class)
                        .setParameter("login", login)
                        .uniqueResult()
        );
    }
}
