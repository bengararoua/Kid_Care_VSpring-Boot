package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.PasswordChangeRequest;  // Importe l'objet de transfert contenant l'ancien et le nouveau mot de passe
import com.kidcare.insight.dto.UserDTO;  // Importe l'objet de transfert contenant les données de l'utilisateur à envoyer au frontend
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import com.kidcare.insight.service.ProfileService;  // Importe le service contenant la logique métier pour la gestion du profil
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, PutMapping, DeleteMapping, RequestBody)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class ProfileController {  // Déclare le contrôleur gérant la gestion du profil utilisateur (mise à jour, changement de mot de passe, suppression de compte)

    @Autowired  // Injecte automatiquement le bean ProfileService géré par Spring
    private ProfileService profileService;  // Service contenant la logique métier pour les opérations sur le profil

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @PutMapping("/profile")  // Associe les requêtes PUT à "/api/profile" à cette méthode
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body,  // Extrait les nouvelles données (name, email) du corps JSON
                                           @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User updatedUser = profileService.updateProfile(user.getUsername(),  // Appelle le service pour mettre à jour le profil (nom et email)
                    body.get("name"), body.get("email"));  // Passe le nouveau nom et le nouvel email (récupérés du corps JSON)
            
            return ResponseEntity.ok(new UserDTO(updatedUser));  // Convertit l'utilisateur mis à jour en DTO (sans le mot de passe) et retourne HTTP 200
            
        } catch (Exception e) {  // Capture toute exception (email déjà utilisé, utilisateur non trouvé, etc.)
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @PutMapping("/password")  // Associe les requêtes PUT à "/api/password" à cette méthode
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest req,  // Extrait les données (ancien mot de passe, nouveau mot de passe) du corps JSON
                                            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            profileService.updatePassword(user.getUsername(),  // Appelle le service pour changer le mot de passe
                    req.getCurrent_password(), req.getPassword());  // Passe l'ancien mot de passe et le nouveau mot de passe
            
            Map<String, String> response = new HashMap<>();  // Crée une map pour stocker le message de confirmation
            response.put("message", "Password updated successfully");  // Ajoute le message de succès
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le message de confirmation
            
        } catch (Exception e) {  // Capture toute exception (ancien mot de passe incorrect, etc.)
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @DeleteMapping("/profile/delete")  // Associe les requêtes DELETE à "/api/profile/delete" à cette méthode
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> body,  // Extrait le mot de passe de confirmation du corps JSON
                                           @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            profileService.deleteAccount(user.getUsername(), body.get("password"));  // Appelle le service pour supprimer le compte (vérifie le mot de passe avant suppression)
            
            Map<String, String> response = new HashMap<>();  // Crée une map pour stocker le message de confirmation
            response.put("message", "Account deleted successfully");  // Ajoute le message de succès
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le message de confirmation
            
        } catch (Exception e) {  // Capture toute exception (mot de passe incorrect, utilisateur non trouvé)
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur dans la map
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }
}