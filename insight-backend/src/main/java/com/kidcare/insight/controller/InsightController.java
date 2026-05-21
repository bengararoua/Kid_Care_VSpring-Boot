package com.kidcare.insight.controller;

import com.kidcare.insight.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    @Autowired private InsightService insightService;

    @GetMapping("/{childId}")
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable Long childId) {
        return ResponseEntity.ok(insightService.getInsights(childId));
    }
}