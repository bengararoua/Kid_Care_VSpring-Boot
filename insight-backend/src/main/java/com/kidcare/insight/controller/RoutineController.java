package com.kidcare.insight.controller;

import com.kidcare.insight.entity.Routine;
import com.kidcare.insight.service.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    @Autowired
    private RoutineService routineService;

    @GetMapping("/{childId}")
    public ResponseEntity<List<Routine>> getRoutines(@PathVariable Long childId, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(routineService.getRoutinesForChild(childId, user.getUsername()));
    }

    @PostMapping("/{childId}")
    public ResponseEntity<Routine> createRoutine(@PathVariable Long childId, @RequestBody Map<String, Object> data, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(routineService.createRoutine(childId, data, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Routine> updateRoutine(@PathVariable Long id, @RequestBody Map<String, Object> data, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(routineService.updateRoutine(id, data, user.getUsername()));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Routine> toggleComplete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(routineService.toggleComplete(id, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoutine(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        routineService.deleteRoutine(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Routine deleted"));
    }
}