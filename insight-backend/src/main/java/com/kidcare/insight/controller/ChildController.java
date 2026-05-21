package com.kidcare.insight.controller;

import com.kidcare.insight.dto.ChildDTO;
import com.kidcare.insight.dto.ChildRequest;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.service.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    @Autowired
    private ChildService childService;

    @GetMapping
    public ResponseEntity<List<ChildDTO>> getChildren(@AuthenticationPrincipal UserDetails user) {
        List<Child> children = childService.getChildrenForUser(user.getUsername());
        List<ChildDTO> dtos = children.stream()
                .map(ChildDTO::fromChild)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> createChild(@RequestBody ChildRequest req, @AuthenticationPrincipal UserDetails user) {
        try {
            Child child = childService.createChild(req, user.getUsername());
            return ResponseEntity.ok(ChildDTO.fromChild(child));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChildDTO> getChild(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Child child = childService.getChildById(id, user.getUsername());
        return ResponseEntity.ok(ChildDTO.fromChild(child));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChild(@PathVariable Long id, @RequestBody ChildRequest req, @AuthenticationPrincipal UserDetails user) {
        try {
            Child child = childService.updateChild(id, req, user.getUsername());
            return ResponseEntity.ok(ChildDTO.fromChild(child));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChild(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        try {
            childService.deleteChild(id, user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Child deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/psychologist")
    public ResponseEntity<?> getPsychologistInfo(@PathVariable Long id) {
        Child child = childService.getChildById(id, null);
        if (child.getPsychologist() != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", child.getPsychologist().getId());
            response.put("name", child.getPsychologist().getName());
            response.put("email", child.getPsychologist().getEmail());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}