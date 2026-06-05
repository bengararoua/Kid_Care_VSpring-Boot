package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.config.JwtUtil;  // Importe l'utilitaire JWT pour générer et valider les tokens
import com.kidcare.insight.dto.LoginRequest;  // Importe l'objet de transfert contenant les identifiants de connexion (email + password)
import com.kidcare.insight.dto.RegisterRequest;  // Importe l'objet de transfert contenant les données d'inscription (name, email, password, role)
import com.kidcare.insight.entity.User;  // Importe l'entité User représentant un utilisateur dans la base de données
import com.kidcare.insight.service.UserService;  // Importe le service contenant la logique métier pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.authentication.AuthenticationManager;  // Importe le gestionnaire d'authentification qui valide email/mot de passe
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // Importe le token contenant les identifiants pour l'authentification
import org.springframework.security.core.Authentication;  // Importe l'interface représentant le résultat de l'authentification
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, PostMapping, etc.)
import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur dynamiques
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class AuthController {  // Déclare le contrôleur gérant l'authentification et l'inscription

    @Autowired private AuthenticationManager authManager;  // Injecte le gestionnaire d'authentification Spring (valide email/mot de passe)
    @Autowired private UserService userService;  // Injecte le service utilisateur pour gérer les inscriptions et recherches
    @Autowired private JwtUtil jwtUtil;  // Injecte l'utilitaire JWT pour générer les tokens après authentification réussie

    @PostMapping("/register")  // Associe les requêtes POST à "/api/register" à cette méthode (inscription d'un nouvel utilisateur)
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {  // Extrait les données d'inscription (name, email, password, role) du corps JSON
        if (userService.findByEmail(req.getEmail()) != null) {  // Vérifie si un utilisateur avec cet email existe déjà en base de données
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));  // Retourne HTTP 400 avec un message d'erreur si l'email est déjà pris
        }
        User user = userService.register(req.getName(), req.getEmail(), req.getPassword(), req.getRole());  // Crée le nouvel utilisateur dans la base de données (mot de passe hashé)
        String token = jwtUtil.generateToken(user.getEmail());  // Génère un token JWT pour l'utilisateur nouvellement inscrit
        Map<String, Object> response = new HashMap<>();  // Crée une map pour construire la réponse JSON
        response.put("token", token);  // Ajoute le token JWT dans la réponse
        response.put("user", Map.of(  // Ajoute les informations de l'utilisateur dans la réponse
                "id", user.getId(),    // ID de l'utilisateur
                "name", user.getName(),  // Nom de l'utilisateur
                "email", user.getEmail(),  // Email de l'utilisateur
                "role", user.getRole()    // Rôle de l'utilisateur (PARENT, EDUCATOR, etc.)
        ));
        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le token et les données utilisateur en JSON
    }

    @PostMapping("/login")  // Associe les requêtes POST à "/api/login" à cette méthode (connexion d'un utilisateur existant)
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {  // Extrait les identifiants (email, password) du corps JSON
        try {  // Bloc try-catch pour capturer les erreurs d'authentification (mauvais mot de passe)
            Authentication auth = authManager.authenticate(  // Valide l'email et le mot de passe auprès de Spring Security
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())  // Crée un token d'authentification avec les identifiants
            );
            User user = userService.findByEmail(req.getEmail());  // Récupère l'utilisateur complet depuis la base de données après authentification réussie
            String token = jwtUtil.generateToken(user.getEmail());  // Génère un nouveau token JWT pour l'utilisateur authentifié
            Map<String, Object> response = new HashMap<>();  // Crée une map pour construire la réponse JSON
            response.put("token", token);  // Ajoute le token JWT dans la réponse
            response.put("user", Map.of(  // Ajoute les informations de l'utilisateur dans la réponse
                    "id", user.getId(),    // ID de l'utilisateur
                    "name", user.getName(),  // Nom de l'utilisateur
                    "email", user.getEmail(),  // Email de l'utilisateur
                    "role", user.getRole()    // Rôle de l'utilisateur
            ));
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le token et les données utilisateur en JSON
        } catch (Exception e) {  // Capture toute exception (mauvais mot de passe, email inexistant)
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));  // Retourne HTTP 401 (Unauthorized) avec un message d'erreur générique
        }
    }

    @PostMapping("/logout")  // Associe les requêtes POST à "/api/logout" à cette méthode (déconnexion)
    public ResponseEntity<?> logout() {  // Note: Avec JWT stateless, aucune action serveur n'est réellement nécessaire
        return ResponseEntity.ok(Map.of("message", "Logged out"));  // Retourne HTTP 200 avec un message de confirmation (le client doit supprimer le token localement)
    }
}