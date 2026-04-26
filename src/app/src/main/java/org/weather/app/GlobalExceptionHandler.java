package org.weather.app;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleSomeException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }
}
