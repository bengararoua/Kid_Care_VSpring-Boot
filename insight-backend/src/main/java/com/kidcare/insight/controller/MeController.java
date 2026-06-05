package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.UserDTO;  // Importe l'objet de transfert qui contient les données de l'utilisateur à envoyer au frontend (sans les données sensibles comme le mot de passe)
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.GetMapping;  // Importe l'annotation pour les requêtes HTTP GET
import org.springframework.web.bind.annotation.RequestMapping;  // Importe l'annotation pour définir le préfixe commun des endpoints
import org.springframework.web.bind.annotation.RestController;  // Importe l'annotation pour déclarer un contrôleur REST

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class MeController {  // Déclare le contrôleur gérant les informations de l'utilisateur connecté (profil personnel)

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @GetMapping("/me")  // Associe les requêtes GET à "/api/me" à cette méthode
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère et retourne les informations de l'utilisateur actuellement connecté
        User user = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base de données par son email (récupéré depuis le token JWT)
            .orElseThrow(() -> new RuntimeException("User not found"));  // Lance une exception si l'utilisateur n'existe pas (cas anormal car le token est valide)
        
        return ResponseEntity.ok(new UserDTO(user));  // Convertit l'entité User en UserDTO (filtre les données sensibles) et retourne HTTP 200 avec les données en JSON
    }
}