package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité BehaviorLog pour les données comportementales
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour les informations de l'enfant
import com.kidcare.insight.repository.BehaviorLogRepository;  // Importe le repository pour les logs
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import java.time.LocalDate;  // Importe la classe LocalDate pour les calculs de dates
import java.util.*;  // Importe les collections Java (HashMap, ArrayList, etc.)
import java.util.stream.Collectors;  // Importe la classe Collectors pour les streams

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class InsightService {  // Déclare le service responsable des analyses statistiques et des insights

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour récupérer les informations de l'enfant

    @Autowired  // Injecte automatiquement le repository BehaviorLogRepository
    private BehaviorLogRepository behaviorLogRepository;  // Repository pour récupérer les logs comportementaux

    // Méthode principale pour récupérer toutes les analyses et insights d'un enfant
    public Map<String, Object> getInsights(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow();  // Récupère l'enfant par son ID (ou exception)
        List<BehaviorLog> logs = behaviorLogRepository.findByChildOrderByLogDateDesc(child);  // Récupère tous les logs triés par date
        
        Map<String, Object> result = new HashMap<>();  // Crée une map pour stocker tous les résultats
        result.put("child_name", child.getName());  // Ajoute le nom de l'enfant
        
        // Cas où aucun log n'existe : retourne des données minimales
        if (logs.isEmpty()) {  // Vérifie si la liste des logs est vide
            result.put("risk_level", "insufficient_data");  // Niveau de risque : données insuffisantes
            result.put("total_logs", 0);  // Total des logs : 0
            result.put("message", "Not enough data");  // Message d'information
            result.put("chart_data", new ArrayList<>());  // Données de graphique : liste vide
            result.put("mood_distribution", new HashMap<>());  // Distribution des humeurs : map vide
            result.put("pattern", new ArrayList<>());  // Patterns : liste vide
            return result;  // Retourne les résultats minimaux
        }
        
        // ===== CALCUL DES MOYENNES =====
        double avgFocus = logs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);  // Moyenne concentration (0 si aucun log)
        double avgSleep = logs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(0);  // Moyenne heures de sommeil
        double avgSocial = logs.stream().mapToInt(BehaviorLog::getSocialInteraction).average().orElse(0);  // Moyenne interactions sociales
        
        result.put("avg_focus", Math.round(avgFocus * 10) / 10.0);  // Ajoute la moyenne concentration (arrondie à 1 décimale)
        result.put("avg_sleep", Math.round(avgSleep * 10) / 10.0);  // Ajoute la moyenne sommeil
        result.put("avg_social", Math.round(avgSocial * 10) / 10.0);  // Ajoute la moyenne sociale
        result.put("total_logs", logs.size());  // Ajoute le nombre total de logs
        
        // ===== CALCUL DU NIVEAU DE RISQUE =====
        String riskLevel = calculateRiskLevel(avgFocus, avgSleep, avgSocial);  // Calcule le risque (low/medium/high)
        result.put("risk_level", riskLevel);  // Ajoute le niveau de risque
        
        // ===== DÉTECTION DES PATTERNS (tendances) =====
        List<String> patterns = detectPatterns(logs);  // Détecte des patterns dans les données (corrélations, évolutions)
        result.put("pattern", patterns);  // Ajoute les patterns détectés
        
        // ===== COMPARAISON HEBDOMADAIRE =====
        Map<String, Object> weeklyComp = getWeeklyComparison(logs);  // Compare la semaine actuelle à la semaine précédente
        result.put("weekly_comparison", weeklyComp);  // Ajoute la comparaison hebdomadaire
        
        // ===== DISTRIBUTION DES HUMEURS =====
        Map<String, Integer> moodDistribution = new HashMap<>();  // Crée une map pour compter les humeurs
        for (BehaviorLog log : logs) {  // Parcourt tous les logs
            moodDistribution.put(log.getMood(), moodDistribution.getOrDefault(log.getMood(), 0) + 1);  // Incrémente le compteur pour cette humeur
        }
        result.put("mood_distribution", moodDistribution);  // Ajoute la distribution des humeurs
        
        // ===== DONNÉES POUR LE GRAPHIQUE (30 derniers jours) =====
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);  // Calcule la date d'il y a 30 jours
        List<BehaviorLog> recentLogs = logs.stream()  // Parcourt tous les logs
            .filter(log -> log.getLogDate().isAfter(thirtyDaysAgo))  // Garde uniquement les logs des 30 derniers jours
            .sorted((a, b) -> a.getLogDate().compareTo(b.getLogDate()))  // Trie par date croissante (plus ancien → plus récent)
            .collect(Collectors.toList());  // Collecte le résultat dans une liste
        
        List<Map<String, Object>> chartData = new ArrayList<>();  // Crée une liste pour les points du graphique
        for (BehaviorLog log : recentLogs) {  // Parcourt chaque log récent
            Map<String, Object> point = new HashMap<>();  // Crée un point de données
            point.put("log_date", log.getLogDate().toString());  // Ajoute la date (format String)
            point.put("focus_level", log.getFocusLevel());  // Ajoute le niveau de concentration
            point.put("sleep_hours", log.getSleepHours());  // Ajoute les heures de sommeil
            point.put("mood", log.getMood());  // Ajoute l'humeur
            point.put("social_interaction", log.getSocialInteraction());  // Ajoute l'interaction sociale
            chartData.add(point);  // Ajoute le point à la liste
        }
        result.put("chart_data", chartData);  // Ajoute les données pour le graphique
        
        return result;  // Retourne la map complète des insights
    }

    // Méthode privée pour calculer le niveau de risque (identique à ActionPlanService)
    private String calculateRiskLevel(double focus, double sleep, double social) {
        int score = 0;  // Initialise le score de risque
        // Score basé sur la concentration
        if (focus < 2.5) score += 2;  // Concentration très faible → +2 points
        else if (focus < 3.5) score += 1;  // Concentration faible → +1 point
        
        // Score basé sur le sommeil
        if (sleep < 6) score += 2;  // Sommeil insuffisant → +2 points
        else if (sleep < 7) score += 1;  // Sommeil limite → +1 point
        
        // Score basé sur les interactions sociales
        if (social < 2.5) score += 2;  // Interactions très faibles → +2 points
        else if (social < 3.5) score += 1;  // Interactions faibles → +1 point
        
        if (score >= 4) return "high";  // Score ≥4 → risque élevé
        if (score >= 2) return "medium";  // Score entre 2 et 3 → risque modéré
        return "low";  // Score <2 → risque faible
    }

    // Méthode privée pour détecter des patterns/tendances dans les données
    private List<String> detectPatterns(List<BehaviorLog> logs) {
        List<String> patterns = new ArrayList<>();  // Crée une liste pour stocker les patterns détectés
        
        // ===== PATTERN 1 : CORRÉLATION SOMMEIL / CONCENTRATION =====
        // Filtre les logs avec peu de sommeil (moins de 7 heures)
        List<BehaviorLog> lowSleepLogs = logs.stream()  // Parcourt tous les logs
            .filter(l -> l.getSleepHours() < 7)  // Garde ceux avec <7h de sommeil
            .collect(Collectors.toList());  // Collecte dans une liste
        
        if (!lowSleepLogs.isEmpty()) {  // Si au moins un log avec peu de sommeil
            double avgFocusLowSleep = lowSleepLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);  // Moyenne concentration pour ces logs
            
            // Filtre les logs avec sommeil normal (≥7 heures)
            List<BehaviorLog> normalSleepLogs = logs.stream()  // Parcourt tous les logs
                .filter(l -> l.getSleepHours() >= 7)  // Garde ceux avec ≥7h de sommeil
                .collect(Collectors.toList());  // Collecte dans une liste
            
            if (!normalSleepLogs.isEmpty()) {  // Si au moins un log avec sommeil normal
                double avgFocusNormalSleep = normalSleepLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);  // Moyenne concentration pour ces logs
                if (avgFocusLowSleep < avgFocusNormalSleep - 0.5) {  // Si la concentration avec peu de sommeil est significativement plus basse
                    patterns.add("Low sleep correlates with lower focus levels");  // Ajoute un pattern en anglais
                }
            }
        }
        
        // ===== PATTERN 2 : ÉVOLUTION RÉCENTE (sur les 14 derniers jours) =====
        if (logs.size() >= 14) {  // Vérifie qu'il y a au moins 14 logs (2 semaines)
            List<BehaviorLog> recentLogs = logs.stream().limit(7).collect(Collectors.toList());  // Prend les 7 plus récents
            List<BehaviorLog> olderLogs = logs.stream().skip(7).limit(7).collect(Collectors.toList());  // Prend les 7 suivants (plus anciens)
            
            double recentAvg = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);  // Moyenne récente
            double olderAvg = olderLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);  // Moyenne ancienne
            
            if (recentAvg < olderAvg - 0.5) {  // Si la moyenne a baissé de plus de 0.5
                patterns.add("Focus level has decreased compared to previous period");  // Détérioration
            } else if (recentAvg > olderAvg + 0.5) {  // Si la moyenne a augmenté de plus de 0.5
                patterns.add("Focus level is improving!");  // Amélioration
            }
        }
        
        return patterns;  // Retourne la liste des patterns détectés
    }

    // Méthode privée pour comparer la semaine actuelle à la semaine précédente
    private Map<String, Object> getWeeklyComparison(List<BehaviorLog> logs) {
        LocalDate now = LocalDate.now();  // Récupère la date actuelle
        
        // Moyenne de concentration de la semaine actuelle (7 derniers jours)
        double currentWeekAvg = logs.stream()  // Parcourt tous les logs
            .filter(l -> l.getLogDate().isAfter(now.minusDays(7)))  // Garde les logs des 7 derniers jours
            .mapToInt(BehaviorLog::getFocusLevel)  // Extrait le niveau de concentration
            .average().orElse(0);  // Calcule la moyenne (0 si aucun log)
        
        // Moyenne de concentration de la semaine précédente (jours 8 à 14)
        double previousWeekAvg = logs.stream()  // Parcourt tous les logs
            .filter(l -> l.getLogDate().isAfter(now.minusDays(14)) && l.getLogDate().isBefore(now.minusDays(7)))  // Garde les logs entre -14 et -7 jours
            .mapToInt(BehaviorLog::getFocusLevel)  // Extrait le niveau de concentration
            .average().orElse(0);  // Calcule la moyenne (0 si aucun log)
        
        Map<String, Object> comp = new HashMap<>();  // Crée une map pour la comparaison
        comp.put("current_week_focus", Math.round(currentWeekAvg * 10) / 10.0);  // Ajoute la moyenne de la semaine actuelle (1 décimale)
        comp.put("previous_week_focus", Math.round(previousWeekAvg * 10) / 10.0);  // Ajoute la moyenne de la semaine précédente
        comp.put("change", Math.round((currentWeekAvg - previousWeekAvg) * 10) / 10.0);  // Ajoute la différence (positive = amélioration)
        return comp;  // Retourne la comparaison
    }
}