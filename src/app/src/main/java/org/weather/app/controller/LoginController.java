package org.weather.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.SessionRepository;
import org.weather.app.repository.UserRepository;
import org.weather.app.service.SessionService;

import java.util.Comparator;
import java.util.List;

@Controller
public class LoginController {
    @Value("${password.pepper}")
    private String pepper;

    @Value("${session.amount}")
    private int sessionAmountPerUser;

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    public LoginController(UserRepository userRepository, SessionRepository sessionRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
    }

    @GetMapping("/login")
    public String get(HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (sessionService.isSessionValid(userSession)) {
            return "redirect:/weather";
        }
        return "login";
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
            return "redirect:/login?errorMessage=" + ErrorMessage.INVALID_PARAMS;
        }

        User foundUser = userRepository.findByLogin(login).orElse(null);
        if (foundUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.USER_NOT_FOUND);
            return "redirect:/login";
        }

        if (!checkPassword(password, foundUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PASSWORD);
            return "redirect:/login";
        }

        handleOldSessions(foundUser);
        UserSession newSession = sessionRepository.save(sessionService.createSession(foundUser));
        response.addCookie(createNewCookie(newSession));

        return "redirect:/weather";
    }

    private Cookie createNewCookie(UserSession newSession) {
        Cookie cookie = new Cookie("sessionId", newSession.getId().toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    private void handleOldSessions(User foundUser) {
        List<UserSession> userSessions = sessionRepository.findAllByUserId(foundUser.getId());

        if (userSessions.size() >= sessionAmountPerUser) {
            List<UserSession> toDelete = userSessions.stream()
                    .sorted(Comparator.comparing(UserSession::getExpires_at))
                    .limit(userSessions.size() - (sessionAmountPerUser-1))
                    .toList();
            sessionRepository.deleteAll(toDelete);
        }
    }

    private boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword + pepper, hashedPassword);
    }
}
