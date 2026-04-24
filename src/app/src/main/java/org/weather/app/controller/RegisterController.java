package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.model.User;
import org.weather.app.repository.UserRepository;

@Controller
public class RegisterController {
    @Value("${password.pepper}")
    private String pepper;

    @Value("${password.rounds}")
    private int rounds;

    private final UserRepository userRepository;

    public RegisterController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String get() {
        return "register";
    }

    @PostMapping("/register")
    public String post(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PARAMS);
            return "redirect:/register";
        }

        if (userRepository.findByLogin(login).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.USER_EXISTS);
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(hashPassword(password));

        userRepository.save(newUser);

        return "redirect:/login";
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password + pepper, BCrypt.gensalt(rounds));
    }
}
