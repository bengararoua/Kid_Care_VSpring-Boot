package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.ChildDTO;  // Importe l'objet de transfert qui contient les données de l'enfant à envoyer au frontend (sans les relations sensibles)
import com.kidcare.insight.dto.ChildRequest;  // Importe l'objet de transfert contenant les données d'entrée pour créer/modifier un enfant (nom, date naissance, etc.)
import com.kidcare.insight.entity.Child;  // Importe l'entité représentant un enfant suivi dans le système
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import com.kidcare.insight.service.ChildService;  // Importe le service contenant la logique métier pour les enfants
import com.kidcare.insight.service.NotificationService;  // Importe le service contenant la logique d'envoi de notifications
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, PutMapping, DeleteMapping)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur
import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur
import java.util.stream.Collectors;  // Importe la classe Collectors pour collecter les résultats des streams Java

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/children")  // Définit le préfixe commun "/api/children" pour tous les endpoints de ce contrôleur
public class ChildController {  // Déclare le contrôleur gérant les opérations CRUD sur les enfants

    @Autowired  // Injecte automatiquement le bean ChildService géré par Spring
    private ChildService childService;  // Déclare le service qui contient la logique métier pour les enfants

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @Autowired  // Injecte automatiquement le bean NotificationService géré par Spring
    private NotificationService notificationService;  // Service pour envoyer des notifications aux utilisateurs

    @GetMapping  // Associe les requêtes GET à "/api/children" à cette méthode
    public ResponseEntity<?> getChildren(@AuthenticationPrincipal UserDetails user) {  // Récupère la liste de tous les enfants associés à l'utilisateur connecté
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            List<Child> children = childService.getChildrenForUser(user.getUsername());  // Appelle le service pour récupérer tous les enfants associés à l'utilisateur (par son rôle: parent, teacher, psychologist)
            
            List<ChildDTO> dtos = children.stream()  // Convertit la liste d'entités Child en flux (stream) pour transformation
                    .map(ChildDTO::fromChild)  // Transforme chaque entité Child en objet ChildDTO (en utilisant la méthode statique fromChild)
                    .collect(Collectors.toList());  // Collecte tous les DTO transformés dans une nouvelle liste
            
            return ResponseEntity.ok(dtos);  // Retourne HTTP 200 (OK) avec la liste des DTO des enfants en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    @PostMapping  // Associe les requêtes POST à "/api/children" à cette méthode
    public ResponseEntity<?> createChild(@RequestBody ChildRequest req,  // Extrait les données du nouvel enfant depuis le corps JSON de la requête
                                         @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            Child child = childService.createChild(req, user.getUsername());  // Appelle le service pour créer un nouvel enfant avec les données fournies, l'associe à l'utilisateur

            User currentUser = userRepository.findByEmail(user.getUsername())  // Cherche l'utilisateur en base de données par son email
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance une exception si l'utilisateur n'existe pas
            
            notificationService.notifyChildAdded(currentUser, child.getName(), child.getId());  // Envoie une notification pour informer de l'ajout de l'enfant (aux personnes concernées comme l'enseignant ou psychologue)

            return ResponseEntity.ok(ChildDTO.fromChild(child));  // Retourne HTTP 200 (OK) avec le DTO de l'enfant créé en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @GetMapping("/{id}")  // Associe les requêtes GET à "/api/children/{id}" à cette méthode
    public ResponseEntity<?> getChild(@PathVariable Long id,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5)
                                      @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            Child child = childService.getChildById(id, user.getUsername());  // Appelle le service pour récupérer l'enfant par son ID (vérifie les droits d'accès)
            return ResponseEntity.ok(ChildDTO.fromChild(child));  // Retourne HTTP 200 (OK) avec le DTO de l'enfant en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }
    
    @PutMapping("/{id}")  // Associe les requêtes PUT à "/api/children/{id}" à cette méthode
    public ResponseEntity<?> updateChild(@PathVariable Long id,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5)
                                         @RequestBody ChildRequest req,  // Extrait les données mises à jour de l'enfant depuis le corps JSON
                                         @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            Child child = childService.updateChild(id, req, user.getUsername());  // Appelle le service pour mettre à jour l'enfant avec les nouvelles données (vérifie les droits)
            return ResponseEntity.ok(ChildDTO.fromChild(child));  // Retourne HTTP 200 (OK) avec le DTO de l'enfant mis à jour en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    @DeleteMapping("/{id}")  // Associe les requêtes DELETE à "/api/children/{id}" à cette méthode
    public ResponseEntity<?> deleteChild(@PathVariable Long id,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5)
                                         @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            childService.deleteChild(id, user.getUsername());  // Appelle le service pour supprimer l'enfant (vérifie les droits avant suppression)
            Map<String, String> response = new HashMap<>();  // Crée une map pour stocker le message de confirmation
            response.put("message", "Child deleted successfully");  // Ajoute le message de succès
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le message de confirmation en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    // Récupérer les infos du psychologue d'un enfant
    @GetMapping("/{id}/psychologist")  // Associe les requêtes GET à "/api/children/{id}/psychologist" à cette méthode
    public ResponseEntity<?> getPsychologistInfo(@PathVariable Long id,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5/psychologist)
                                                 @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
                error.put("error", "Utilisateur non authentifié");  // Ajoute le message d'erreur
                return ResponseEntity.status(401).body(error);  // Retourne HTTP 401 (Unauthorized) avec message d'erreur
            }

            Child child = childService.getChildById(id, user.getUsername());  // Récupère l'enfant par son ID (vérifie les droits d'accès)
            
            if (child.getPsychologist() != null) {  // Vérifie si l'enfant a un psychologue associé
                Map<String, Object> response = new HashMap<>();  // Crée une map pour stocker les infos du psychologue
                response.put("id", child.getPsychologist().getId());  // Ajoute l'ID du psychologue
                response.put("name", child.getPsychologist().getName());  // Ajoute le nom du psychologue
                response.put("email", child.getPsychologist().getEmail());  // Ajoute l'email du psychologue
                return ResponseEntity.ok(response);  // Retourne HTTP 200 avec les informations du psychologue en JSON
            }
            return ResponseEntity.notFound().build();  // Retourne HTTP 404 (Not Found) si aucun psychologue n'est associé
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }

    // Récupérer les infos de l'enseignant d'un enfant
    @GetMapping("/{id}/teacher")  // Associe les requêtes GET à "/api/children/{id}/teacher" à cette méthode
    public ResponseEntity<?> getTeacherInfo(@PathVariable Long id,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5/teacher)
                                            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
                error.put("error", "Utilisateur non authentifié");  // Ajoute le message d'erreur
                return ResponseEntity.status(401).body(error);  // Retourne HTTP 401 (Unauthorized) avec message d'erreur
            }

            Child child = childService.getChildById(id, user.getUsername());  // Récupère l'enfant par son ID (vérifie les droits d'accès)
            
            if (child.getTeacher() != null) {  // Vérifie si l'enfant a un enseignant associé
                Map<String, Object> response = new HashMap<>();  // Crée une map pour stocker les infos de l'enseignant
                response.put("id", child.getTeacher().getId());  // Ajoute l'ID de l'enseignant
                response.put("name", child.getTeacher().getName());  // Ajoute le nom de l'enseignant
                response.put("email", child.getTeacher().getEmail());  // Ajoute l'email de l'enseignant
                return ResponseEntity.ok(response);  // Retourne HTTP 200 avec les informations de l'enseignant en JSON
            }
            return ResponseEntity.notFound().build();  // Retourne HTTP 404 (Not Found) si aucun enseignant n'est associé
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.status(403).body(error);  // Retourne HTTP 403 (Forbidden) avec le message d'erreur
        }
    }
}