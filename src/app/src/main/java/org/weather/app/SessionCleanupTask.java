package org.weather.app;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weather.app.repository.SessionRepository;

import java.time.LocalDateTime;

@Component
public class SessionCleanupTask {
    private final SessionRepository sessionRepository;

    public SessionCleanupTask(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(fixedRateString = "${session.cleanup.rate.ms}")
    public void cleanExpiredSessions() {
        sessionRepository.deleteExpired(LocalDateTime.now());
    }
}
