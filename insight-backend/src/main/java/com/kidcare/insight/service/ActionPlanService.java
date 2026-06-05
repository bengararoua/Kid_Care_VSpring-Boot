package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.ActionPlan;  // Importe l'entité ActionPlan pour la création et la gestion des plans
import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité BehaviorLog pour analyser les journaux de comportement
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour identifier l'enfant concerné
import com.kidcare.insight.repository.ActionPlanRepository;  // Importe le repository pour les plans d'action
import com.kidcare.insight.repository.BehaviorLogRepository;  // Importe le repository pour les journaux de comportement
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un composant Spring de type Service
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDate;  // Importe la classe LocalDate pour la date de génération
import java.util.*;  // Importe les collections Java (List, ArrayList, HashMap, Arrays)

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class ActionPlanService {  // Déclare le service responsable de la génération des plans d'action personnalisés

    @Autowired  // Injecte automatiquement le repository ActionPlanRepository
    private ActionPlanRepository actionPlanRepository;  // Repository pour sauvegarder et récupérer les plans

    @Autowired  // Injecte automatiquement le repository BehaviorLogRepository
    private BehaviorLogRepository behaviorLogRepository;  // Repository pour récupérer les journaux de comportement

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour récupérer les informations de l'enfant

    @Transactional  // Exécute la méthode dans une transaction (annule en cas d'erreur)
    public ActionPlan generatePlan(Long childId) {  // Génère un plan d'action personnalisé basé sur les logs récents
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🔍 generatePlan - childId: " + childId);  // Log de début de génération

            Child child = childRepository.findById(childId)  // Cherche l'enfant par son ID
                    .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));  // Lance une exception si non trouvé

            System.out.println("✅ Child trouvé: " + child.getName());  // Log de confirmation

            // Récupération des 7 derniers logs (ou moins si pas assez)
            List<BehaviorLog> recentLogs = new ArrayList<>();  // Initialise une liste pour les logs récents
            try {
                recentLogs = behaviorLogRepository.findTop7ByChildOrderByLogDateDesc(child);  // Tente de récupérer les 7 derniers logs
                System.out.println("📊 Logs récupérés: " + recentLogs.size());  // Log du nombre de logs trouvés
            } catch (Exception e) {  // Si la méthode Top7 échoue (ancienne version de base de données)
                recentLogs = behaviorLogRepository.findByChildOrderByLogDateDesc(child);  // Récupère tous les logs triés
                if (recentLogs.size() > 7) {  // Si plus de 7 logs
                    recentLogs = recentLogs.subList(0, 7);  // Garde seulement les 7 premiers (les plus récents)
                }
                System.out.println("📊 Logs récupérés (fallback): " + recentLogs.size());  // Log du nombre avec méthode alternative
            }

            // Si aucun log n'existe, création d'un plan par défaut
            if (recentLogs.isEmpty()) {  // Vérifie si la liste des logs est vide
                System.out.println("📝 Aucun log, création d'un plan par défaut");  // Log d'information
                return createDefaultPlan(child);  // Crée et retourne un plan par défaut
            }

            // Calcul des moyennes à partir des logs récents
            double avgFocus = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(3);  // Moyenne du niveau de concentration (défaut 3)
            double avgSleep = recentLogs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(8);  // Moyenne des heures de sommeil (défaut 8)
            double avgSocial = recentLogs.stream().mapToInt(BehaviorLog::getSocialInteraction).average().orElse(3);  // Moyenne des interactions sociales (défaut 3)

            System.out.println("📊 Moyennes - Focus: " + avgFocus + ", Sleep: " + avgSleep + ", Social: " + avgSocial);  // Log des moyennes

            // Calcul du niveau de risque basé sur les moyennes
            String riskLevel = calculateRiskLevel(avgFocus, avgSleep, avgSocial);  // Appelle la méthode de calcul du risque
            System.out.println("⚠️ Niveau de risque: " + riskLevel);  // Log du niveau de risque

            // Récupération du plan d'action selon le niveau de risque
            Map<String, List<String>> planData = getPlanByRiskLevel(riskLevel);  // Récupère les activités recommandées

            // Vérifie si un plan existe déjà pour aujourd'hui
            ActionPlan existingPlan = null;  // Initialise le plan existant à null
            try {
                existingPlan = actionPlanRepository  // Cherche le dernier plan généré pour cet enfant
                        .findTopByChildIdOrderByGeneratedDateDesc(childId)  // Récupère le plan le plus récent
                        .orElse(null);  // Retourne null si aucun plan n'existe
            } catch (Exception e) {  // En cas d'erreur
                System.out.println("⚠️ Pas de plan existant: " + e.getMessage());  // Log d'information
            }

            // Détermine s'il faut créer un nouveau plan ou mettre à jour l'existant
            ActionPlan plan;  // Déclare la variable plan
            if (existingPlan != null && existingPlan.getGeneratedDate() != null &&
                    existingPlan.getGeneratedDate().equals(LocalDate.now())) {  // Si un plan existe déjà pour aujourd'hui
                plan = existingPlan;  // Utilise le plan existant
                System.out.println("📝 Mise à jour du plan existant");  // Log de mise à jour
            } else {  // Sinon
                plan = new ActionPlan();  // Crée un nouveau plan
                plan.setChild(child);  // Associe l'enfant au plan
                System.out.println("📝 Création d'un nouveau plan");  // Log de création
            }

            // Remplissage des champs du plan avec les activités recommandées
            plan.setRiskLevel(riskLevel);  // Définit le niveau de risque
            plan.setGeneratedDate(LocalDate.now());  // Définit la date de génération (aujourd'hui)

            plan.setMorningActivities(cleanActivityString(String.join("||", planData.getOrDefault("morning", Arrays.asList("Aucune activité")))));  // Définit les activités du matin (séparées par ||)
            plan.setAfternoonActivities(cleanActivityString(String.join("||", planData.getOrDefault("afternoon", Arrays.asList("Aucune activité")))));  // Définit les activités de l'après-midi
            plan.setEveningActivities(cleanActivityString(String.join("||", planData.getOrDefault("evening", Arrays.asList("Aucune activité")))));  // Définit les activités du soir
            plan.setCommunicationTips(cleanActivityString(String.join("||", planData.getOrDefault("communication", Arrays.asList("Aucun conseil")))));  // Définit les conseils de communication
            plan.setGamesActivities(cleanActivityString(String.join("||", planData.getOrDefault("games", Arrays.asList("Aucun jeu")))));  // Définit les jeux recommandés

            ActionPlan savedPlan = actionPlanRepository.save(plan);  // Sauvegarde le plan en base de données
            System.out.println("✅ Plan sauvegardé avec ID: " + savedPlan.getId());  // Log de confirmation

            return savedPlan;  // Retourne le plan sauvegardé

        } catch (Exception e) {  // Capture toute exception
            System.err.println(" Erreur dans generatePlan: " + e.getMessage());  // Log d'erreur
            e.printStackTrace();  // Affiche la trace complète de l'erreur
            throw new RuntimeException("Erreur lors de la génération du plan: " + e.getMessage(), e);  // Lance une RuntimeException avec le message
        }
    }

    // Méthode privée pour nettoyer la chaîne d'activités (remplace <br> par ||, supprime les puces, etc.)
    private String cleanActivityString(String input) {
        if (input == null) return null;  // Si l'entrée est nulle, retourne null

        // Remplacer <br> par ||
        String cleaned = input.replaceAll("<br\\s*/?>", "||");  // Remplace les balises <br> par ||
        // Supprimer les •
        cleaned = cleaned.replaceAll("[•●]\\s*", "");  // Supprime les puces et leurs espaces
        // Nettoyer les doubles ||
        cleaned = cleaned.replaceAll("\\|\\|\\|+", "||");  // Remplace les |||| par || (évite les doublons)
        // Supprimer les || au début et à la fin
        cleaned = cleaned.replaceAll("^\\|\\|", "").replaceAll("\\|\\|$", "");  // Supprime les séparateurs en bordure
        // Supprimer les espaces autour
        cleaned = cleaned.trim();  // Supprime les espaces au début et à la fin

        return cleaned;  // Retourne la chaîne nettoyée
    }

    // Méthode privée pour calculer le niveau de risque à partir des moyennes
    private String calculateRiskLevel(double focus, double sleep, double social) {
        int score = 0;  // Initialise le score de risque à 0
        
        // Calcul du score basé sur la concentration
        if (focus < 2.5) score += 2;  // Concentration très faible → +2 points
        else if (focus < 3.5) score += 1;  // Concentration faible → +1 point
        
        // Calcul du score basé sur le sommeil
        if (sleep < 6) score += 2;  // Sommeil insuffisant → +2 points
        else if (sleep < 7) score += 1;  // Sommeil limite → +1 point
        
        // Calcul du score basé sur les interactions sociales
        if (social < 2.5) score += 2;  // Interactions très faibles → +2 points
        else if (social < 3.5) score += 1;  // Interactions faibles → +1 point

        if (score >= 4) return "high";  // Score ≥4 → risque élevé
        if (score >= 2) return "medium";  // Score entre 2 et 3 → risque modéré
        return "low";  // Score <2 → risque faible
    }

    // Méthode privée qui retourne les activités recommandées selon le niveau de risque
    private Map<String, List<String>> getPlanByRiskLevel(String riskLevel) {
        Map<String, List<String>> plans = new HashMap<>();  // Crée une map pour stocker les activités par catégorie

        // Plan pour risque FAIBLE
        if ("low".equals(riskLevel)) {
            plans.put("morning", Arrays.asList(  // Activités du matin pour risque faible
                    "🌅 Réveil en douceur avec 5 minutes de câlins",
                    "🥣 Petit-déjeuner équilibré ensemble",
                    "📋 Faire une liste des tâches de la journée avec l'enfant"));
            plans.put("afternoon", Arrays.asList(  // Activités de l'après-midi pour risque faible
                    "🎨 Activité créative (dessin, peinture, pâte à modeler)",
                    "🏃‍♂️ 30 minutes de jeu en extérieur",
                    "📚 Lecture d'une histoire ensemble"));
            plans.put("evening", Arrays.asList(  // Activités du soir pour risque faible
                    "🍽️ Dîner en famille sans écrans",
                    "🛁 Rituel du bain relaxant",
                    "📖 Histoire du soir et câlins"));
            plans.put("communication", Arrays.asList(  // Conseils de communication pour risque faible
                    "🗣️ Poser des questions ouvertes sur sa journée",
                    "👂 Écouter activement sans jugement",
                    "🎉 Féliciter les efforts, pas seulement les résultats"));
            plans.put("games", Arrays.asList(  // Jeux recommandés pour risque faible
                    "🧩 Jeux de société éducatifs",
                    "🎲 Memory ou puzzles",
                    "🎭 Jeux d'imitation"));
        } 
        // Plan pour risque MODÉRÉ
        else if ("medium".equals(riskLevel)) {
            plans.put("morning", Arrays.asList(  // Activités du matin pour risque modéré
                    "🌅 Réveil progressif avec musique douce",
                    "🥣 Petit-déjeuner protéiné pour l'énergie",
                    "📝 Établir un planning visuel de la journée"));
            plans.put("afternoon", Arrays.asList(  // Activités de l'après-midi pour risque modéré
                    "🧘 Exercices de respiration (5 minutes)",
                    "🎯 Jeux de concentration (Lego, puzzle)",
                    "🚶‍♂️ Promenade calme dans la nature"));
            plans.put("evening", Arrays.asList(  // Activités du soir pour risque modéré
                    "🍽️ Dîner calme sans stimulation",
                    "🛁 Bain aux huiles essentielles (lavande)",
                    "📖 Lecture calmante et discussion"));
            plans.put("communication", Arrays.asList(  // Conseils de communication pour risque modéré
                    "💬 Utiliser des phrases courtes et claires",
                    "😊 Valider ses émotions ('Je comprends que tu sois...')",
                    "🎯 Proposer des choix limités (2-3 options)"));
            plans.put("games", Arrays.asList(  // Jeux recommandés pour risque modéré
                    "🎯 Jeux d'équilibre",
                    "🧘 Yoga pour enfants",
                    "🎨 Coloriage mandalas"));
        } 
        // Plan pour risque ÉLEVÉ
        else {
            plans.put("morning", Arrays.asList(  // Activités du matin pour risque élevé
                    "🌅 Réveil anticipé pour éviter la pression",
                    "🥣 Petit-déjeuner riche en protéines",
                    "📋 Routine visuelle avec pictogrammes"));
            plans.put("afternoon", Arrays.asList(  // Activités de l'après-midi pour risque élevé
                    "🛑 Créer un coin calme avec coussins",
                    "🤗 Activité sensorielle (bouteille magique, sable)",
                    "👂 Écoute de musique relaxante"));
            plans.put("evening", Arrays.asList(  // Activités du soir pour risque élevé
                    "🌙 Réduire les écrans 2h avant le coucher",
                    "🛏️ Rituel de coucher structuré",
                    "📝 Journal de gratitude"));
            plans.put("communication", Arrays.asList(  // Conseils de communication pour risque élevé
                    "🤝 Utiliser le toucher rassurant",
                    "📏 Parler avec un ton calme et bas",
                    "⏰ Donner des avertissements avant les transitions"));
            plans.put("games", Arrays.asList(  // Jeux recommandés pour risque élevé
                    "🎭 Jeux de rôle pour exprimer les émotions",
                    "🧸 Jeu avec pâte à modeler anti-stress",
                    "🃏 Cartes des émotions"));
        }
        return plans;  // Retourne la map des activités
    }

    // Méthode privée pour créer un plan par défaut (quand pas assez de données)
    private ActionPlan createDefaultPlan(Child child) {
        ActionPlan plan = new ActionPlan();  // Crée un nouveau plan
        plan.setChild(child);  // Associe l'enfant
        plan.setRiskLevel("low");  // Définit le risque par défaut comme faible
        plan.setGeneratedDate(LocalDate.now());  // Définit la date de génération
        plan.setMorningActivities("📝 Pas assez de données pour générer un plan personnalisé");  // Message par défaut
        plan.setAfternoonActivities("📝 Continuez à enregistrer des logs quotidiens");  // Message par défaut
        plan.setEveningActivities("📝 7 jours de logs sont nécessaires pour un plan complet");  // Message par défaut
        plan.setCommunicationTips("💬 Enregistrez plus de données pour des conseils personnalisés");  // Message par défaut
        plan.setGamesActivities("🎮 Revenez après quelques jours de suivi");  // Message par défaut
        return actionPlanRepository.save(plan);  // Sauvegarde et retourne le plan par défaut
    }

    // Méthode publique pour récupérer le dernier plan généré d'un enfant
    public ActionPlan getLatestPlan(Long childId) {
        try {  // Bloc try-catch pour capturer les erreurs
            System.out.println("🔍 getLatestPlan - childId: " + childId);  // Log de début de recherche
            ActionPlan plan = actionPlanRepository  // Cherche le dernier plan
                    .findTopByChildIdOrderByGeneratedDateDesc(childId)  // Récupère le plan le plus récent
                    .orElse(null);  // Retourne null si aucun plan n'existe
            if (plan == null) {  // Si aucun plan n'est trouvé
                System.out.println("ℹ️ Aucun plan trouvé pour childId: " + childId);  // Log d'information
            } else {  // Sinon
                System.out.println("✅ Plan trouvé - ID: " + plan.getId() + ", Date: " + plan.getGeneratedDate());  // Log de confirmation
            }
            return plan;  // Retourne le plan (ou null)
        } catch (Exception e) {  // Capture toute exception
            System.err.println(" Erreur dans getLatestPlan: " + e.getMessage());  // Log d'erreur
            e.printStackTrace();  // Affiche la trace
            return null;  // Retourne null en cas d'erreur
        }
    }
}