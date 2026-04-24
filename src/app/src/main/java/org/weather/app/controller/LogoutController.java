package org.weather.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;
import org.weather.app.service.SessionService;

@Controller
public class LogoutController {
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;

    public LogoutController(SessionService sessionService, SessionRepository sessionRepository) {
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        UserSession sessionToDelete = sessionService.getSessionFromCookie(request.getCookies());
        if (sessionToDelete != null) {
            sessionRepository.delete(sessionToDelete);
        }

        response.addCookie(resetCookie());

        return "redirect:/login";
    }

    private Cookie resetCookie() {
        Cookie cookie = new Cookie("sessionId", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
