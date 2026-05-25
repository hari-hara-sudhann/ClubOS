package com.codeygen.clubos.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class HashPassword {
    private final PasswordEncoder passwordEncoder;

    public String hashPassword(String raw) {
        return passwordEncoder.encode(raw);
    }
    public boolean verifyPassword(String raw, String hashed) {
        return passwordEncoder.matches(raw, hashed);
    }
}
