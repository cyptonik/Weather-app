package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.model.User;
import org.weather.app.repository.UserRepository;

@Controller
public class RegisterController {
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
        if (login.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PARAMS);
            return "redirect:/register";
        }

        if (userRepository.findByLogin(login).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.USER_EXISTS);
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(password);

        userRepository.save(newUser);

        return "redirect:/login";
    }
}
