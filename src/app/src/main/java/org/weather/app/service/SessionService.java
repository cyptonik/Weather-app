package org.weather.app.service;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

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
