package com.kidcare.insight.controller;

import com.kidcare.insight.entity.Appointment;
import com.kidcare.insight.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForUser(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Map<String, Object> data, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(appointmentService.createAppointment(data, user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id, user.getUsername()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> data, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, data.get("status"), user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        appointmentService.deleteAppointment(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Appointment deleted"));
    }
}