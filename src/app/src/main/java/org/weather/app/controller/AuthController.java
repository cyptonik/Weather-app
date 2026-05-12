package org.weather.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.weather.app.ErrorMessage;
import org.weather.app.dto.LoginRequest;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;
import org.weather.app.repository.UserRepository;
import org.weather.app.service.AuthService;
import org.weather.app.service.SessionService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, SessionService sessionService, SessionRepository sessionRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.authService = authService;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletResponse response) {
        User user = userRepository.findByLogin(req.login()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.resolve(401), ErrorMessage.USER_NOT_FOUND);
        }
        if (!authService.checkPassword(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.resolve(401), ErrorMessage.INVALID_PASSWORD);
        }

        UserSession session = sessionService.handleOldSessions(user);
        response.addCookie(createCookie(session));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        String loginErr = authService.checkLogin(req.login());
        if (loginErr != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, loginErr);
        }

        String passErr = authService.checkPassword(req.password());
        if (passErr != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, passErr);
        }

        if (userRepository.findByLogin(req.login()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.resolve(409), ErrorMessage.USER_EXISTS);
        }

        User user = new User();
        user.setLogin(req.login());
        user.setPassword(authService.hashPassword(req.password()));
        userRepository.save(user);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        UserSession session = sessionService.getSessionFromCookie(request.getCookies());
        if (session != null) {
            sessionRepository.delete(session);
        }
        response.addCookie(resetCookie());
        return ResponseEntity.ok().build();
    }

    private Cookie resetCookie() {
        Cookie cookie = new Cookie("sessionId", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }

    private Cookie createCookie(UserSession newSession) {
        Cookie cookie = new Cookie("sessionId", newSession.getId().toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
