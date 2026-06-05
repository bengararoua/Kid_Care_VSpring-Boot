package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.service.InsightService;  // Importe le service contenant la logique métier pour les analyses et insights des enfants
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PathVariable)
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/insights")  // Définit le préfixe commun "/api/insights" pour tous les endpoints de ce contrôleur
public class InsightController {  // Déclare le contrôleur gérant les analyses et insights (statistiques, tendances, recommandations) pour les enfants

    @Autowired private InsightService insightService;  // Injecte automatiquement le bean InsightService géré par Spring (contient la logique d'analyse)

    @GetMapping("/{childId}")  // Associe les requêtes GET à "/api/insights/{childId}" à cette méthode
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable Long childId) {  // Récupère les analyses et insights pour un enfant spécifique (ex: tendances de comportement, progression, recommandations)
        return ResponseEntity.ok(insightService.getInsights(childId));  // Appelle le service pour générer les insights et retourne HTTP 200 (OK) avec la map des données en JSON
    }
}