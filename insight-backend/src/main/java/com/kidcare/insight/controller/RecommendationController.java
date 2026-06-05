package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.RecommendationRequest;  // Importe l'objet de transfert contenant les données d'une recommandation (titre, description, catégorie)
import com.kidcare.insight.entity.Recommendation;  // Importe l'entité représentant une recommandation personnalisée pour un enfant
import com.kidcare.insight.service.RecommendationService;  // Importe le service contenant la logique métier pour les recommandations
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.HttpStatus;  // Importe les codes de statut HTTP (201 Created, 401 Unauthorized, etc.)
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, PutMapping, DeleteMapping, CrossOrigin)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur
import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
@CrossOrigin(origins = "http://localhost:3000")  // Autorise les requêtes cross-origin provenant du frontend React (port 3000)
public class RecommendationController {  // Déclare le contrôleur gérant les recommandations personnalisées pour les enfants

    @Autowired  // Injecte automatiquement le bean RecommendationService géré par Spring
    private RecommendationService recommendationService;  // Service contenant la logique métier pour les recommandations

    @GetMapping("/children/{childId}/recommendations")  // Associe les requêtes GET à "/api/children/{childId}/recommendations" à cette méthode
    public ResponseEntity<?> getRecommendations(  // Récupère toutes les recommandations pour un enfant spécifique
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /children/5/recommendations)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🔵 GET /children/" + childId + "/recommendations");  // Log de débogage avec emoji bleu
            
            List<Recommendation> recommendations = recommendationService.getRecommendationsForChild(childId, user.getUsername());  // Appelle le service pour récupérer toutes les recommandations de l'enfant (vérifie les droits)
            
            List<Map<String, Object>> response = new java.util.ArrayList<>();  // Crée une liste pour stocker les recommandations formatées en map
            for (Recommendation rec : recommendations) {  // Parcourt chaque recommandation récupérée
                Map<String, Object> recMap = new HashMap<>();  // Crée une map pour une recommandation individuelle
                recMap.put("id", rec.getId());  // Ajoute l'ID de la recommandation
                recMap.put("title", rec.getTitle());  // Ajoute le titre de la recommandation
                recMap.put("description", rec.getDescription());  // Ajoute la description détaillée
                recMap.put("category", rec.getCategory());  // Ajoute la catégorie (communication, social, attention, comportement)
                recMap.put("completed", rec.isCompleted());  // Ajoute l'état d'avancement (true si complétée, false sinon)
                recMap.put("createdAt", rec.getCreatedAt() != null ? rec.getCreatedAt().toString() : null);  // Ajoute la date de création (ou null)
                recMap.put("authorName", rec.getAuthor() != null ? rec.getAuthor().getName() : null);  // Ajoute le nom de l'auteur qui a créé la recommandation
                response.add(recMap);  // Ajoute la recommandation formatée à la liste de réponse
            }
            
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec la liste des recommandations en JSON

        } catch (Exception e) {  // Capture toute exception (erreur de permission, enfant non trouvé)
            System.err.println(" Erreur GET recommendations: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.ok(new java.util.ArrayList<>());  // Retourne HTTP 200 (OK) avec une liste vide (fallback silencieux)
        }
    }

    @PostMapping("/children/{childId}/recommendations")  // Associe les requêtes POST à "/api/children/{childId}/recommendations" à cette méthode
    public ResponseEntity<?> addRecommendation(  // Ajoute une nouvelle recommandation pour un enfant
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL
            @RequestBody RecommendationRequest req,  // Extrait les données de la recommandation (titre, description, catégorie) du corps JSON
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("📝 POST /children/" + childId + "/recommendations");  // Log de débogage avec emoji crayon
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            Recommendation recommendation = recommendationService.addRecommendation(childId, req, user.getUsername());  // Appelle le service pour créer la recommandation (parent/enseignant/psychologue)
            
            Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("id", recommendation.getId());  // Ajoute l'ID de la recommandation créée
            response.put("title", recommendation.getTitle());  // Ajoute le titre
            response.put("description", recommendation.getDescription());  // Ajoute la description
            response.put("category", recommendation.getCategory());  // Ajoute la catégorie
            response.put("completed", recommendation.isCompleted());  // Ajoute l'état (false par défaut)
            response.put("createdAt", recommendation.getCreatedAt());  // Ajoute la date de création
            response.put("authorName", recommendation.getAuthor().getName());  // Ajoute le nom de l'auteur
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);  // Retourne HTTP 201 (Created) avec la recommandation créée en JSON

        } catch (Exception e) {  // Capture toute exception (enfant non trouvé, non autorisé)
            System.err.println(" Erreur POST recommendation: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @PutMapping("/recommendations/{id}/toggle")  // Associe les requêtes PUT à "/api/recommendations/{id}/toggle" à cette méthode
    public ResponseEntity<?> toggleRecommendation(  // Inverse l'état "completed" d'une recommandation (non complété ↔ complété)
            @PathVariable Long id,  // Extrait l'ID de la recommandation depuis l'URL (ex: /recommendations/5/toggle)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🔄 PUT /recommendations/" + id + "/toggle");  // Log de débogage avec emoji rotation
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            Recommendation recommendation = recommendationService.toggleRecommendation(id, user.getUsername());  // Appelle le service pour inverser l'état completed (true/false)
            
            return ResponseEntity.ok(Map.of(  // Retourne HTTP 200 (OK) avec l'ID et le nouvel état
                "id", recommendation.getId(),  // ID de la recommandation
                "completed", recommendation.isCompleted()  // Nouvel état (true si maintenant complété)
            ));

        } catch (Exception e) {  // Capture toute exception (recommandation non trouvée, non autorisé)
            System.err.println(" Erreur toggle: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @DeleteMapping("/recommendations/{id}")  // Associe les requêtes DELETE à "/api/recommendations/{id}" à cette méthode
    public ResponseEntity<?> deleteRecommendation(  // Supprime une recommandation spécifique
            @PathVariable Long id,  // Extrait l'ID de la recommandation depuis l'URL (ex: /recommendations/5)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🗑️ DELETE /recommendations/" + id);  // Log de débogage avec emoji poubelle
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            recommendationService.deleteRecommendation(id, user.getUsername());  // Appelle le service pour supprimer la recommandation (vérifie que l'utilisateur est l'auteur ou a les droits)
            return ResponseEntity.ok(Map.of("message", "Recommandation supprimée"));  // Retourne HTTP 200 (OK) avec un message de confirmation

        } catch (Exception e) {  // Capture toute exception (non autorisé, recommandation non trouvée)
            System.err.println(" Erreur delete: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }
}