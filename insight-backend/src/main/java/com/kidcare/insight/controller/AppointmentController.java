package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.entity.Appointment;  // Importe l'entité Appointment représentant un rendez-vous
import com.kidcare.insight.service.AppointmentService;  // Importe le service contenant la logique métier pour les rendez-vous
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, etc.)
import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/appointments")  // Définit le préfixe commun "/api/appointments" pour tous les endpoints de ce contrôleur
public class AppointmentController {  // Déclare le contrôleur gérant les requêtes liées aux rendez-vous

    @Autowired  // Injecte automatiquement le bean AppointmentService géré par Spring
    private AppointmentService appointmentService;  // Déclare le service qui contient la logique métier pour les rendez-vous

    @GetMapping  // Associe les requêtes GET à "/api/appointments" à cette méthode (récupère tous les rendez-vous)
    public ResponseEntity<List<Appointment>> getAppointments(@AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(appointmentService.getAppointmentsForUser(user.getUsername()));  // Appelle le service pour récupérer tous les rendez-vous de l'utilisateur et retourne HTTP 200 avec la liste en JSON
    }

    @PostMapping  // Associe les requêtes POST à "/api/appointments" à cette méthode (crée un nouveau rendez-vous)
    public ResponseEntity<Appointment> createAppointment(@RequestBody Map<String, Object> data,  // Extrait les données JSON du corps de la requête (titre, date, description, etc.)
                                                         @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(appointmentService.createAppointment(data, user.getUsername()));  // Appelle le service pour créer un nouveau rendez-vous et retourne HTTP 200 avec l'objet créé en JSON
    }

    @GetMapping("/{id}")  // Associe les requêtes GET à "/api/appointments/{id}" à cette méthode (récupère un rendez-vous spécifique)
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long id,  // Extrait l'ID du rendez-vous depuis l'URL (ex: /5)
                                                      @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(appointmentService.getAppointmentById(id, user.getUsername()));  // Appelle le service pour récupérer le rendez-vous par son ID (vérifie les droits) et retourne HTTP 200
    }

    @PatchMapping("/{id}/status")  // Associe les requêtes PATCH à "/api/appointments/{id}/status" à cette méthode (met à jour le statut)
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id,  // Extrait l'ID du rendez-vous depuis l'URL
                                                    @RequestBody Map<String, String> data,  // Extrait les données JSON du corps (contient le nouveau statut: "confirmé", "annulé", etc.)
                                                    @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(appointmentService.updateStatus(id, data.get("status"), user.getUsername()));  // Appelle le service pour mettre à jour le statut et retourne HTTP 200 avec l'objet mis à jour
    }

    @DeleteMapping("/{id}")  // Associe les requêtes DELETE à "/api/appointments/{id}" à cette méthode (supprime un rendez-vous)
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id,  // Extrait l'ID du rendez-vous depuis l'URL
                                               @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        appointmentService.deleteAppointment(id, user.getUsername());  // Appelle le service pour supprimer le rendez-vous (vérifie les droits avant suppression)
        return ResponseEntity.ok(Map.of("message", "Appointment deleted"));  // Retourne HTTP 200 avec un message de confirmation en JSON
    }
}