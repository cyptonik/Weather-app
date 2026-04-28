package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.model.User;
import org.weather.app.repository.UserRepository;
import org.weather.app.service.PasswordService;
import org.weather.app.service.RegistrationService;

@Controller
public class RegisterController {
    private final RegistrationService registrationService;

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public RegisterController(UserRepository userRepository, RegistrationService registrationService, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.passwordService = passwordService;
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

        String invalidLoginMessage = registrationService.checkLogin(login);
        if (invalidLoginMessage != null) {
            redirectAttributes.addFlashAttribute("errorMessage", invalidLoginMessage);
            return "redirect:/register";
        }

        String invalidPasswordMessage = registrationService.checkPassword(password);
        if (invalidPasswordMessage != null) {
            redirectAttributes.addFlashAttribute("errorMessage", invalidPasswordMessage);
            return "redirect:/register";
        }

        if (userRepository.findByLogin(login).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.USER_EXISTS);
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(passwordService.hashPassword(password));

        userRepository.save(newUser);

        return "redirect:/login";
    }
}
