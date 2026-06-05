package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application
import com.kidcare.insight.entity.ActionPlan;  // Importe l'entité ActionPlan représentant un plan d'action personnalisé
import com.kidcare.insight.service.ActionPlanService;  // Importe le service contenant la logique métier pour les plans d'action
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.HttpStatus;  // Importe les codes de statut HTTP (200, 401, 404, 500, etc.)
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes (status + body)
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, etc.)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur dynamiques
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (combinaison de @Controller + @ResponseBody)
@RequestMapping("/api/action-plan")  // Définit le préfixe commun pour tous les endpoints de ce contrôleur
public class ActionPlanController {  // Déclare le contrôleur gérant les requêtes liées aux plans d'action

    @Autowired  // Injecte automatiquement le bean ActionPlanService géré par Spring
    private ActionPlanService actionPlanService;  // Déclare le service qui contient la logique de génération des plans

    @GetMapping("/{childId}/generate")  // Associe les requêtes GET à "/api/action-plan/{childId}/generate" à cette méthode
    public ResponseEntity<?> generatePlan(@PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5/generate)
                                          @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs potentielles
            if (userDetails == null) {  // Vérifie si aucun utilisateur n'est authentifié
                Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
                error.put("error", "Utilisateur non authentifié");  // Ajoute le message d'erreur dans la map
                return ResponseEntity.status(401).body(error);  // Retourne une réponse HTTP 401 (Unauthorized) avec le message d'erreur
            }

            System.out.println("📋 Génération du plan pour l'enfant ID: " + childId);  // Affiche un log de débogage (enfant ciblé)
            System.out.println("👤 Utilisateur: " + userDetails.getUsername());  // Affiche un log de débogage (utilisateur connecté)

            ActionPlan plan = actionPlanService.generatePlan(childId);  // Appelle le service pour générer un nouveau plan d'action pour l'enfant

            // Convertir en Map pour le frontend (car l'entité peut avoir des problèmes de sérialisation JSON)
            Map<String, Object> response = new HashMap<>();  // Crée une map pour construire la réponse JSON
            response.put("id", plan.getId());  // Ajoute l'ID du plan dans la réponse
            response.put("riskLevel", plan.getRiskLevel());  // Ajoute le niveau de risque (faible, moyen, élevé)
            response.put("generatedDate", plan.getGeneratedDate());  // Ajoute la date de génération du plan
            response.put("morningActivities", plan.getMorningActivities());  // Ajoute les activités recommandées le matin
            response.put("afternoonActivities", plan.getAfternoonActivities());  // Ajoute les activités recommandées l'après-midi
            response.put("eveningActivities", plan.getEveningActivities());  // Ajoute les activités recommandées le soir
            response.put("communicationTips", plan.getCommunicationTips());  // Ajoute les conseils de communication pour les parents
            response.put("gamesActivities", plan.getGamesActivities());  // Ajoute les jeux et activités ludiques recommandés

            return ResponseEntity.ok(response);  // Retourne une réponse HTTP 200 (OK) avec les données du plan en JSON
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            e.printStackTrace();  // Affiche la trace complète de l'erreur dans les logs serveur
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message de l'exception dans la map
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);  // Retourne une réponse HTTP 500 (Erreur serveur)
        }
    }

    @GetMapping("/{childId}/latest")  // Associe les requêtes GET à "/api/action-plan/{childId}/latest" à cette méthode
    public ResponseEntity<?> getLatestPlan(@PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5/latest)
                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs potentielles
            if (userDetails == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).build();  // Retourne une réponse HTTP 401 (Unauthorized) sans body
            }

            ActionPlan plan = actionPlanService.getLatestPlan(childId);  // Appelle le service pour récupérer le dernier plan généré pour l'enfant
            if (plan == null) {  // Vérifie si aucun plan n'existe pour cet enfant
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Aucun plan trouvé"));  // Retourne HTTP 404 avec message d'erreur
            }

            // Convertir en Map pour le frontend (même structure que l'endpoint generate)
            Map<String, Object> response = new HashMap<>();  // Crée une map pour construire la réponse JSON
            response.put("id", plan.getId());  // Ajoute l'ID du plan dans la réponse
            response.put("riskLevel", plan.getRiskLevel());  // Ajoute le niveau de risque
            response.put("generatedDate", plan.getGeneratedDate());  // Ajoute la date de génération
            response.put("morningActivities", plan.getMorningActivities());  // Ajoute les activités du matin
            response.put("afternoonActivities", plan.getAfternoonActivities());  // Ajoute les activités de l'après-midi
            response.put("eveningActivities", plan.getEveningActivities());  // Ajoute les activités du soir
            response.put("communicationTips", plan.getCommunicationTips());  // Ajoute les conseils de communication
            response.put("gamesActivities", plan.getGamesActivities());  // Ajoute les jeux recommandés

            return ResponseEntity.ok(response);  // Retourne une réponse HTTP 200 (OK) avec les données du plan en JSON
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            e.printStackTrace();  // Affiche la trace complète de l'erreur dans les logs serveur
            Map<String, String> error = new HashMap<>();  // Crée une map pour stocker le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message de l'exception dans la map
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);  // Retourne une réponse HTTP 500 (Erreur serveur)
        }
    }
}