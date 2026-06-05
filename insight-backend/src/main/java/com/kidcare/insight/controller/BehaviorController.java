package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.BehaviorLogRequest;  // Importe l'objet de transfert contenant les données d'un journal de comportement (date, description, humeur, etc.)
import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité représentant un journal de comportement enregistré en base de données
import com.kidcare.insight.service.BehaviorService;  // Importe le service contenant la logique métier pour les journaux de comportement
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.HttpStatus;  // Importe les codes de statut HTTP (200, 201, 403, 404, 500, etc.)
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes (status + body)
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, DeleteMapping, etc.)

import java.util.*;  // Importe toutes les collections Java (List, ArrayList, HashMap, Map, etc.)

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class BehaviorController {  // Déclare le contrôleur gérant les journaux de comportement (logs) des enfants

    @Autowired  // Injecte automatiquement le bean BehaviorService géré par Spring
    private BehaviorService behaviorService;  // Déclare le service qui contient la logique métier pour les journaux de comportement

    @GetMapping("/logs/{childId}")  // Associe les requêtes GET à "/api/logs/{childId}" à cette méthode
    public ResponseEntity<?> getLogs(@PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /logs/5)
                                     @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            List<BehaviorLog> logs = behaviorService.getLogsForChild(childId, user.getUsername());  // Appelle le service pour récupérer tous les journaux de comportement de l'enfant spécifié
            return ResponseEntity.ok(logs);  // Retourne HTTP 200 (OK) avec la liste des journaux en JSON
            
        } catch (RuntimeException e) {  // Capture les exceptions d'exécution (erreurs métier)
            // Pour les nouveaux comptes ou utilisateurs non autorisés, retourner une liste vide
            if (e.getMessage().contains("not associated") || e.getMessage().contains("No child found")) {  // Vérifie si l'utilisateur n'est pas associé à l'enfant ou si l'enfant n'existe pas
                return ResponseEntity.ok(new ArrayList<>());  // Retourne HTTP 200 avec une liste vide (pas d'erreur, juste aucun log)
            }
            // Pour les autres erreurs (utilisateur non trouvé, etc.)
            Map<String, String> errorResponse = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            errorResponse.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    @PostMapping("/logs")  // Associe les requêtes POST à "/api/logs" à cette méthode
    public ResponseEntity<?> createLog(@RequestBody BehaviorLogRequest req,  // Extrait les données du journal depuis le corps JSON de la requête
                                       @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            BehaviorLog log = behaviorService.createLog(req, user.getUsername());  // Appelle le service pour créer un nouveau journal de comportement avec les données fournies
            return ResponseEntity.ok(log);  // Retourne HTTP 200 (OK) avec l'objet BehaviorLog créé (contenant son ID généré) en JSON
            
        } catch (RuntimeException e) {  // Capture les exceptions d'exécution (erreurs métier)
            Map<String, String> errorResponse = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            errorResponse.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    @DeleteMapping("/logs/{id}")  // Associe les requêtes DELETE à "/api/logs/{id}" à cette méthode
    public ResponseEntity<?> deleteLog(@PathVariable Long id,  // Extrait l'ID du journal depuis l'URL (ex: /logs/42)
                                       @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            behaviorService.deleteLog(id, user.getUsername());  // Appelle le service pour supprimer le journal d'ID spécifié (vérifie les droits avant suppression)
            Map<String, String> response = new HashMap<>();  // Crée une map pour le message de confirmation
            response.put("message", "Log deleted successfully");  // Ajoute le message de succès dans la map
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le message de confirmation en JSON
            
        } catch (RuntimeException e) {  // Capture les exceptions d'exécution (erreurs métier)
            Map<String, String> errorResponse = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            errorResponse.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }
}