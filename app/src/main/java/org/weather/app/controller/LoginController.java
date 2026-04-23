package org.weather.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String get() {
        return "login";
    }

    @PostMapping("/login")
    public String post() {
        return "login";
    }
}
