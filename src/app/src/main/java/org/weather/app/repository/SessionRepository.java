package org.weather.app.repository;

import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.weather.app.model.UserSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class SessionRepository {
    private final SessionFactory sessionFactory;

    public SessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public UserSession save(UserSession userSession) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(userSession);
        session.flush();
        return userSession;
    }

    public List<UserSession> findAllByUserId(Integer id) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM UserSession WHERE user.id = :id", UserSession.class)
                .setParameter("id", id)
                .list();
    }

    public Optional<UserSession> findById(UUID uuid) {
        return Optional.ofNullable(sessionFactory.getCurrentSession()
                .createQuery("FROM UserSession WHERE id = :id", UserSession.class)
                .setParameter("id", uuid)
                .uniqueResult());
    }

    public void delete(UserSession userSession) {
        sessionFactory.getCurrentSession().remove(userSession);
    }

    public void deleteAll(List<UserSession> userSessions) {
        Session session = sessionFactory.getCurrentSession();
        for (UserSession userSession : userSessions) {
            session.remove(userSession);
        }
    }

    public void deleteExpired(LocalDateTime now) {
        sessionFactory.getCurrentSession()
                .createMutationQuery("DELETE FROM UserSession WHERE expires_at < :now")
                .setParameter("now", now)
                .executeUpdate();
    }

}
