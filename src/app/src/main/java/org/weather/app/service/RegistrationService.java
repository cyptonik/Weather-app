package org.weather.app.service;

import org.springframework.stereotype.Service;
import org.weather.app.ErrorMessage;

@Service
public class RegistrationService {
    public String checkLogin(String login) {
        if (login.length() < 4 || login.length() > 30) {
            return ErrorMessage.INVALID_LOGIN_LENGTH;
        }

        if (!login.matches("^[a-zA-Z].*")) {
            return ErrorMessage.INVALID_LOGIN_FIRST_SYMBOL;
        }

        if (!login.matches("[a-zA-Z0-9_-]+")) {
            return ErrorMessage.INVALID_LOGIN_SPECIAL_SYMBOLS;
        }

        return null;
    }

    public String checkPassword(String password) {
        if (password.length() < 8 || password.length() > 40) {
            return ErrorMessage.INVALID_PASSWORD_LENGTH;
        }
        if (!password.matches(".*[A-Z].*")) {
            return ErrorMessage.INVALID_PASSWORD_UPPERCASE;
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return ErrorMessage.INVALID_PASSWORD_SPECIAL;
        }
        return null;
    }
}
