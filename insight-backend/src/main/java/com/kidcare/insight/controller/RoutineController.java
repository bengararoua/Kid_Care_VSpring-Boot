package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.RoutineDTO;  // Importe l'objet de transfert contenant les données d'une routine (activité, horaire, complétée, etc.)
import com.kidcare.insight.entity.Child;  // Importe l'entité représentant un enfant suivi dans le système
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository JPA pour accéder aux enfants en base
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux utilisateurs en base
import com.kidcare.insight.service.RoutineService;  // Importe le service contenant la logique métier pour les routines
import com.kidcare.insight.service.NotificationService;  // Importe le service contenant la logique d'envoi de notifications
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, PutMapping, PatchMapping, DeleteMapping)

import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/routines")  // Définit le préfixe commun "/api/routines" pour tous les endpoints de ce contrôleur
public class RoutineController {  // Déclare le contrôleur gérant les routines quotidiennes pour les enfants (matin, après-midi, soir)

    @Autowired  // Injecte automatiquement le bean RoutineService géré par Spring
    private RoutineService routineService;  // Service contenant la logique métier pour les routines

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux utilisateurs en base de données

    @Autowired  // Injecte automatiquement le bean ChildRepository géré par Spring
    private ChildRepository childRepository;  // Repository pour accéder aux enfants en base de données

    @Autowired  // Injecte automatiquement le bean NotificationService géré par Spring
    private NotificationService notificationService;  // Service pour envoyer des notifications (rappel de routine)

    @GetMapping("/{childId}")  // Associe les requêtes GET à "/api/routines/{childId}" à cette méthode
    public ResponseEntity<List<RoutineDTO>> getRoutines(  // Récupère toutes les routines pour un enfant spécifique
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /routines/5)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(routineService.getRoutinesForChild(childId, user.getUsername()));  // Appelle le service pour récupérer toutes les routines de l'enfant (vérifie les droits) et retourne HTTP 200 avec la liste en JSON
    }

    @PostMapping("/{childId}")  // Associe les requêtes POST à "/api/routines/{childId}" à cette méthode
    public ResponseEntity<RoutineDTO> createRoutine(  // Crée une nouvelle routine pour un enfant
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL
            @RequestBody Map<String, Object> data,  // Extrait les données de la routine (activity, timeOfDay, completed, etc.) du corps JSON
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT

        RoutineDTO routine = routineService.createRoutine(childId, data, user.getUsername());  // Appelle le service pour créer la routine (vérifie les droits)

        try {  // Bloc try-catch pour capturer les erreurs de notification (ne bloque pas la création)
            User currentUser = userRepository.findByEmail(user.getUsername())  // Cherche l'utilisateur en base par son email
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
            
            Child child = childRepository.findById(childId)  // Cherche l'enfant en base par son ID
                    .orElseThrow(() -> new RuntimeException("Child not found"));  // Lance exception si non trouvé

            String activityName = routine.getActivity();  // Récupère le nom de l'activité de la routine
            if (activityName == null || activityName.isEmpty()) {  // Vérifie si le nom d'activité est vide
                activityName = "routine";  // Utilise un nom par défaut "routine"
            }

            notificationService.notifyRoutineReminder(child, activityName, routine.getId());  // Envoie une notification de rappel pour la routine (ex: "N'oubliez pas la routine de brossage de dents")
        } catch (Exception e) {  // Capture toute exception lors de l'envoi de la notification
            System.err.println("Error creating notification: " + e.getMessage());  // Log l'erreur (ne bloque pas la création de la routine)
        }

        return ResponseEntity.ok(routine);  // Retourne HTTP 200 (OK) avec la routine créée en JSON
    }

    @PutMapping("/{id}")  // Associe les requêtes PUT à "/api/routines/{id}" à cette méthode
    public ResponseEntity<RoutineDTO> updateRoutine(  // Met à jour une routine existante
            @PathVariable Long id,  // Extrait l'ID de la routine depuis l'URL (ex: /routines/42)
            @RequestBody Map<String, Object> data,  // Extrait les nouvelles données de la routine (activity, timeOfDay) du corps JSON
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(routineService.updateRoutine(id, data, user.getUsername()));  // Appelle le service pour mettre à jour la routine (vérifie les droits) et retourne HTTP 200 avec la routine mise à jour
    }

    @PatchMapping("/{id}/toggle")  // Associe les requêtes PATCH à "/api/routines/{id}/toggle" à cette méthode
    public ResponseEntity<RoutineDTO> toggleComplete(  // Inverse l'état "completed" d'une routine (non complété ↔ complété)
            @PathVariable Long id,  // Extrait l'ID de la routine depuis l'URL (ex: /routines/42/toggle)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        return ResponseEntity.ok(routineService.toggleComplete(id, user.getUsername()));  // Appelle le service pour inverser l'état completed et retourne HTTP 200 avec la routine mise à jour
    }

    @DeleteMapping("/{id}")  // Associe les requêtes DELETE à "/api/routines/{id}" à cette méthode
    public ResponseEntity<?> deleteRoutine(  // Supprime une routine spécifique
            @PathVariable Long id,  // Extrait l'ID de la routine depuis l'URL (ex: /routines/42)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        routineService.deleteRoutine(id, user.getUsername());  // Appelle le service pour supprimer la routine (vérifie les droits)
        return ResponseEntity.ok(Map.of("message", "Routine deleted"));  // Retourne HTTP 200 (OK) avec un message de confirmation
    }
}