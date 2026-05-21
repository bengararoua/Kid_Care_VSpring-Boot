package com.kidcare.insight.controller;

import com.kidcare.insight.dto.BehaviorLogRequest;
import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.service.BehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BehaviorController {

    @Autowired private BehaviorService behaviorService;

    @GetMapping("/logs/{childId}")
    public ResponseEntity<List<BehaviorLog>> getLogs(@PathVariable Long childId, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(behaviorService.getLogsForChild(childId, user.getUsername()));
    }

    @PostMapping("/logs")
    public ResponseEntity<BehaviorLog> createLog(@RequestBody BehaviorLogRequest req, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(behaviorService.createLog(req, user.getUsername()));
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        behaviorService.deleteLog(id, user.getUsername());
        return ResponseEntity.ok().build();
    }
}