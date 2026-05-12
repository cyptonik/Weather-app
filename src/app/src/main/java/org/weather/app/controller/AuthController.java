package org.weather.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.weather.app.model.UserSession;
import org.weather.app.service.SessionService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SessionService sessionService;

    public AuthController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginReq) {
        // ... проверка логина/пароля ...
        UserSession session = sessionService.handleOldSessions(foundUser);

        // Возвращаем JSON с ID сессии, чтобы фронтенд его сохранил (в LocalStorage или куках)
        return ResponseEntity.ok(Map.of("sessionId", session.getId().toString()));
    }
}
