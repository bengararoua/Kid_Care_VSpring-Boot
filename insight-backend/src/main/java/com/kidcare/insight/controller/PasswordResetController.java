package com.kidcare.insight.controller;

import com.kidcare.insight.dto.PasswordEmailRequest;
import com.kidcare.insight.dto.PasswordResetRequest;
import com.kidcare.insight.dto.TokenCheckRequest;
import com.kidcare.insight.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired private PasswordResetService passwordResetService;

    @PostMapping("/email")
    public ResponseEntity<?> sendResetLink(@RequestBody PasswordEmailRequest req) {
        String token = passwordResetService.createResetToken(req.getEmail());
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email not found"));
        }
        return ResponseEntity.ok(Map.of("message", "Reset token generated", "token", token, "email", req.getEmail()));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest req) {
        boolean success = passwordResetService.resetPassword(req.getToken(), req.getEmail(), req.getPassword());
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/check-token")
    public ResponseEntity<Map<String, Boolean>> checkToken(@RequestBody TokenCheckRequest req) {
        boolean valid = passwordResetService.validateToken(req.getToken(), req.getEmail());
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}