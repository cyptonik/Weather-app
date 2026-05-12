package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.weather.app.model.UserSession;
import org.weather.app.service.SessionService;

@Controller
public class IndexController {
    private final SessionService sessionService;

    public IndexController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (sessionService.isSessionValid(userSession)) {
            return "redirect:/weather";
        }

        return "old2/index";
    }
}
