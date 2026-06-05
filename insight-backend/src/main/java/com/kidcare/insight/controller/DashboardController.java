package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur dynamiques
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/dashboard")  // Définit le préfixe commun "/api/dashboard" pour tous les endpoints de ce contrôleur
public class DashboardController {  // Déclare le contrôleur gérant les statistiques et informations du tableau de bord utilisateur

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @GetMapping("/stats")  // Associe les requêtes GET à "/api/dashboard/stats" à cette méthode
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère les statistiques du tableau de bord pour l'utilisateur connecté
        User user = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base de données par son email
            .orElseThrow(() -> new RuntimeException("User not found"));  // Lance une exception si l'utilisateur n'existe pas
        
        Map<String, Object> stats = new HashMap<>();  // Crée une map pour stocker toutes les statistiques à retourner
        
        //  Utilisation des méthodes isParent(), isTeacher(), isPsychologist()
        if (user.isParent()) {  // Vérifie si l'utilisateur connecté a le rôle PARENT
            stats.put("role", "parent");  // Ajoute le rôle "parent" dans les statistiques
            stats.put("childrenCount", 0); // À implémenter selon ta logique (nombre d'enfants associés au parent)
            stats.put("points", user.getPoints());  // Ajoute les points de gamification (récompenses, niveau) de l'utilisateur
            
        } else if (user.isTeacher()) {  // Vérifie si l'utilisateur connecté a le rôle ENSEIGNANT
            stats.put("role", "teacher");  // Ajoute le rôle "teacher" dans les statistiques
            stats.put("studentsCount", 0); // À implémenter (nombre d'élèves associés à l'enseignant)
            
        } else if (user.isPsychologist()) {  // Vérifie si l'utilisateur connecté a le rôle PSYCHOLOGUE
            stats.put("role", "psychologist");  // Ajoute le rôle "psychologist" dans les statistiques
            stats.put("appointmentsCount", 0); // À implémenter (nombre de rendez-vous programmés)
        }
        
        stats.put("name", user.getName());  // Ajoute le nom complet de l'utilisateur dans les statistiques
        stats.put("email", user.getEmail());  // Ajoute l'email de l'utilisateur dans les statistiques
        
        return ResponseEntity.ok(stats);  // Retourne HTTP 200 (OK) avec la map des statistiques en JSON
    }
}