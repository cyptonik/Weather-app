package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.weather.app.model.UserSession;
import org.weather.app.service.SessionService;

@Controller
public class WeatherController {
    private final SessionService sessionService;

    public WeatherController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/weather")
    public String get(HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(userSession)) {
            return "redirect:/login";
        }

        request.setAttribute("login", userSession.getUser().getLogin());
        return "weather";
    }
}
