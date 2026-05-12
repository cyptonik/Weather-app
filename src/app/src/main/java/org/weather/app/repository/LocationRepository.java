package org.weather.app.repository;

import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.weather.app.model.Location;

import java.util.List;

@Repository
@Transactional
public class LocationRepository {
    private final SessionFactory sessionFactory;

    public LocationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Location save(Location location) {
        sessionFactory.getCurrentSession().persist(location);
        return location;
    }

    public List<Location> findAllByUserId(Integer id) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Location WHERE user.id = :id", Location.class)
                .setParameter("id", id)
                .list();
    }

    public Location findById(Integer id) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Location WHERE id = :id", Location.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    public void delete(Location location) {
        sessionFactory.getCurrentSession().remove(location);
    }
}
