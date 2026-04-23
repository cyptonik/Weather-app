package org.weather.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {
    @Value("${session.duration.seconds}")
    private int sessionDurationSeconds;

    @Value("${session.duration.minutes}")
    private int sessionDurationMinutes;

    @Value("${session.duration.hours}")
    private int sessionDurationHours;

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
}
