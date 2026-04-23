package org.weather.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Controller
public class WeatherController {
    private final SessionRepository sessionRepository;

    public WeatherController(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/weather")
    public String get(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        UUID uuid = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("sessionId"))
                .map(Cookie::getValue)
                .findFirst()
                .map(UUID::fromString)
                .orElse(null);

        if (uuid == null) {
            return "redirect:/login";
        }

        UserSession userSession = sessionRepository.findById(uuid).orElse(null);
        if (userSession == null || userSession.getExpires_at().isBefore(LocalDateTime.now())) {
            return "redirect:/login";
        }

        request.setAttribute("login", userSession.getUser().getLogin());
        return "weather";
    }
}
