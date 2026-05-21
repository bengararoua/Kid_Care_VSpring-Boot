package com.kidcare.insight.service;

import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, ResetToken> resetTokens = new HashMap<>();

    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, new ResetToken(email, LocalDateTime.now().plusHours(1)));
        return token;
    }

    public boolean validateToken(String token, String email) {
        ResetToken rt = resetTokens.get(token);
        if (rt == null) return false;
        if (!rt.email.equals(email)) return false;
        if (LocalDateTime.now().isAfter(rt.expiry)) return false;
        return true;
    }

    public boolean resetPassword(String token, String email, String newPassword) {
        if (!validateToken(token, email)) return false;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokens.remove(token);
        return true;
    }

    private static class ResetToken {
        String email;
        LocalDateTime expiry;
        ResetToken(String email, LocalDateTime expiry) {
            this.email = email;
            this.expiry = expiry;
        }
    }
}