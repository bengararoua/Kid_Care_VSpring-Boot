package com.kidcare.insight.controller;

import com.kidcare.insight.entity.ActionPlan;
import com.kidcare.insight.service.ActionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/action-plan")
public class ActionPlanController {

    @Autowired
    private ActionPlanService actionPlanService;

    @GetMapping("/{childId}/generate")
    public ResponseEntity<ActionPlan> generatePlan(@PathVariable Long childId) {
        return ResponseEntity.ok(actionPlanService.generatePlan(childId));
    }

    @GetMapping("/{childId}/latest")
    public ResponseEntity<ActionPlan> getLatestPlan(@PathVariable Long childId) {
        ActionPlan plan = actionPlanService.getLatestPlan(childId);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }
}