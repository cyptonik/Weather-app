package org.weather.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.UserRepository;
import org.weather.app.service.PasswordService;
import org.weather.app.service.SessionService;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordService passwordService;

    public LoginController(UserRepository userRepository, SessionService sessionService, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.passwordService = passwordService;
    }

    @GetMapping("/login")
    public String get(HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (sessionService.isSessionValid(userSession)) {
            return "redirect:/weather";
        }
        return "old2/login";
    }

    @PostMapping("/login")
    public String post(RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (sessionService.isSessionValid(userSession)) {
            return "redirect:/weather";
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PARAMS);
            return "redirect:/login";
        }

        User foundUser = userRepository.findByLogin(login).orElse(null);
        if (foundUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.USER_NOT_FOUND);
            return "redirect:/login";
        }

        if (!passwordService.checkPassword(password, foundUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PASSWORD);
            return "redirect:/login";
        }

        response.addCookie(createNewCookie(sessionService.handleOldSessions(foundUser)));

        return "redirect:/weather";
    }

    private Cookie createNewCookie(UserSession newSession) {
        Cookie cookie = new Cookie("sessionId", newSession.getId().toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
