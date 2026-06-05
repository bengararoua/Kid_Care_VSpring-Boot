package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.PasswordEmailRequest;  // Importe l'objet de transfert contenant l'email pour la demande de réinitialisation
import com.kidcare.insight.dto.PasswordResetRequest;  // Importe l'objet de transfert contenant le token, l'email et le nouveau mot de passe
import com.kidcare.insight.dto.TokenCheckRequest;  // Importe l'objet de transfert contenant le token et l'email pour vérifier la validité
import com.kidcare.insight.service.PasswordResetService;  // Importe le service contenant la logique métier pour la réinitialisation des mots de passe
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, PostMapping, RequestBody)
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/password")  // Définit le préfixe commun "/api/password" pour tous les endpoints de ce contrôleur
public class PasswordResetController {  // Déclare le contrôleur gérant la réinitialisation des mots de passe (mot de passe oublié)

    @Autowired private PasswordResetService passwordResetService;  // Injecte automatiquement le bean PasswordResetService géré par Spring (contient la logique de réinitialisation)

    @PostMapping("/email")  // Associe les requêtes POST à "/api/password/email" à cette méthode
    public ResponseEntity<?> sendResetLink(@RequestBody PasswordEmailRequest req) {  // Envoie un lien de réinitialisation à l'email fourni (ou génère un token en développement)
        String token = passwordResetService.createResetToken(req.getEmail());  // Appelle le service pour créer un token de réinitialisation (envoie un email ou retourne le token directement)
        
        if (token == null) {  // Vérifie si le token n'a pas été créé (email non trouvé en base)
            return ResponseEntity.badRequest().body(Map.of("error", "Email not found"));  // Retourne HTTP 400 (Bad Request) avec un message d'erreur
        }
        
        return ResponseEntity.ok(Map.of("message", "Reset token generated", "token", token, "email", req.getEmail()));  // Retourne HTTP 200 (OK) avec le token (pour développement) et un message de confirmation
    }

    @PostMapping("/reset")  // Associe les requêtes POST à "/api/password/reset" à cette méthode
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest req) {  // Réinitialise le mot de passe avec le token, l'email et le nouveau mot de passe
        boolean success = passwordResetService.resetPassword(req.getToken(), req.getEmail(), req.getPassword());  // Appelle le service pour valider le token et mettre à jour le mot de passe
        
        if (!success) {  // Vérifie si la réinitialisation a échoué (token invalide, expiré, ou email ne correspond pas)
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));  // Retourne HTTP 400 (Bad Request) avec un message d'erreur
        }
        
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));  // Retourne HTTP 200 (OK) avec un message de confirmation
    }

    @PostMapping("/check-token")  // Associe les requêtes POST à "/api/password/check-token" à cette méthode
    public ResponseEntity<Map<String, Boolean>> checkToken(@RequestBody TokenCheckRequest req) {  // Vérifie si un token de réinitialisation est encore valide
        boolean valid = passwordResetService.validateToken(req.getToken(), req.getEmail());  // Appelle le service pour vérifier la validité du token (non expiré, correspond à l'email)
        return ResponseEntity.ok(Map.of("valid", valid));  // Retourne HTTP 200 (OK) avec un booléen "valid" (true si le token est valide, false sinon)
    }
}