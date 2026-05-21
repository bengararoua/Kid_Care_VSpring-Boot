package com.kidcare.insight.controller;

import com.kidcare.insight.dto.RecommendationRequest;
import com.kidcare.insight.entity.Recommendation;
import com.kidcare.insight.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    @Autowired private RecommendationService recommendationService;

    @GetMapping("/children/{childId}/recommendations")
    public ResponseEntity<List<Recommendation>> getRecommendations(@PathVariable Long childId, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForChild(childId, user.getUsername()));
    }

    @PostMapping("/children/{childId}/recommendations")
    public ResponseEntity<Recommendation> addRecommendation(@PathVariable Long childId, @RequestBody RecommendationRequest req, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(recommendationService.addRecommendation(childId, req, user.getUsername()));
    }

    @PutMapping("/recommendations/{id}/toggle")
    public ResponseEntity<Recommendation> toggleComplete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(recommendationService.toggleComplete(id, user.getUsername()));
    }

    @DeleteMapping("/recommendations/{id}")
    public ResponseEntity<?> deleteRecommendation(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        recommendationService.deleteRecommendation(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Recommendation deleted"));
    }
}