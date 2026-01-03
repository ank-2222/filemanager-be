package com.file.manager.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptUtil {
    @Autowired
    private  PasswordEncoder passwordEncoder;

    public String hashPassword(String password) {
       return passwordEncoder.encode(password);

    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
