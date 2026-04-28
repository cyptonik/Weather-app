package org.weather.app.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    @Value("${password.pepper}")
    private String pepper;

    @Value("${password.rounds}")
    private int rounds;

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword + pepper, hashedPassword);
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password + pepper, BCrypt.gensalt(rounds));
    }
}
