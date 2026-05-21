package com.kidcare.insight.controller;

import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/psychologists")
    public ResponseEntity<List<User>> getPsychologists() {
        return ResponseEntity.ok(userRepository.findByRole(User.Role.PSYCHOLOGIST));
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getTeachers() {
        return ResponseEntity.ok(userRepository.findByRole(User.Role.TEACHER));
    }

    @GetMapping("/parents")
    public ResponseEntity<List<User>> getParents() {
        return ResponseEntity.ok(userRepository.findByRole(User.Role.PARENT));
    }
}