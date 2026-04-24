package org.weather.app.service;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    @Value("${session.amount}")
    private int sessionAmountPerUser;

    @Value("${session.duration.seconds}")
    private int sessionDurationSeconds;

    @Value("${session.duration.minutes}")
    private int sessionDurationMinutes;

    @Value("${session.duration.hours}")
    private int sessionDurationHours;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public UserSession createSession(User user) {
        UserSession userSession = new UserSession();
        userSession.setExpires_at(LocalDateTime.now()
                .plusSeconds(sessionDurationSeconds)
                .plusMinutes(sessionDurationMinutes)
                .plusHours(sessionDurationHours));

        userSession.setUser(user);
        userSession.setId(UUID.randomUUID());

        return userSession;
    }

    public UserSession getSessionFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        UUID uuid = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("sessionId"))
                .map(Cookie::getValue)
                .findFirst()
                .map(UUID::fromString)
                .orElse(null);
        if (uuid == null) {
            return null;
        }

        return sessionRepository.findById(uuid).orElse(null);
    }

    public UserSession handleOldSessions(User foundUser) {
        List<UserSession> userSessions = sessionRepository.findAllByUserId(foundUser.getId());

        if (userSessions.size() >= sessionAmountPerUser) {
            List<UserSession> toDelete = userSessions.stream()
                    .sorted(Comparator.comparing(UserSession::getExpires_at))
                    .limit(userSessions.size() - (sessionAmountPerUser-1))
                    .toList();
            sessionRepository.deleteAll(toDelete);
        }
        return sessionRepository.save(createSession(foundUser));
    }

    public boolean isSessionValid(UserSession session) {
        if (session == null) {
            return false;
        }
        if (session.getExpires_at().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            return false;
        }
        return true;
    }
}
