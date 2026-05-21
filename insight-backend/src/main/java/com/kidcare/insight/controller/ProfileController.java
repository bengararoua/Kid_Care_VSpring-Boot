package com.kidcare.insight.controller;

import com.kidcare.insight.dto.PasswordChangeRequest;
import com.kidcare.insight.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired private ProfileService profileService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(profileService.updateProfile(user.getUsername(), body.get("name"), body.get("email")));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest req, @AuthenticationPrincipal UserDetails user) {
        profileService.updatePassword(user.getUsername(), req.getCurrentPassword(), req.getPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    @PostMapping("/profile/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails user) {
        profileService.deleteAccount(user.getUsername(), body.get("password"));
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }
}