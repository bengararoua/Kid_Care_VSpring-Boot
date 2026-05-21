package com.kidcare.insight.service;

import com.kidcare.insight.entity.ActionPlan;
import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.repository.ActionPlanRepository;
import com.kidcare.insight.repository.BehaviorLogRepository;
import com.kidcare.insight.repository.ChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class ActionPlanService {

    @Autowired
    private ActionPlanRepository actionPlanRepository;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private ChildRepository childRepository;

    public ActionPlan generatePlan(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        List<BehaviorLog> recentLogs = behaviorLogRepository.findTop7ByChildOrderByLogDateDesc(child);

        if (recentLogs.isEmpty()) {
            return createDefaultPlan(child);
        }

        double avgFocus = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(3);
        double avgSleep = recentLogs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(8);
        double avgSocial = recentLogs.stream().mapToInt(BehaviorLog::getSocialInteraction).average().orElse(3);

        String riskLevel = calculateRiskLevel(avgFocus, avgSleep, avgSocial);
        Map<String, List<String>> planData = getPlanByRiskLevel(riskLevel);

        ActionPlan plan = new ActionPlan();
        plan.setChild(child);
        plan.setRiskLevel(riskLevel);
        plan.setGeneratedDate(LocalDate.now());
        plan.setMorningActivities(String.join("||", planData.get("morning")));
        plan.setAfternoonActivities(String.join("||", planData.get("afternoon")));
        plan.setEveningActivities(String.join("||", planData.get("evening")));
        plan.setCommunicationTips(String.join("||", planData.get("communication")));
        plan.setGamesActivities(String.join("||", planData.get("games")));

        return actionPlanRepository.save(plan);
    }

    private String calculateRiskLevel(double focus, double sleep, double social) {
        int score = 0;
        if (focus < 2.5) score += 2;
        else if (focus < 3.5) score += 1;
        if (sleep < 6) score += 2;
        else if (sleep < 7) score += 1;
        if (social < 2.5) score += 2;
        else if (social < 3.5) score += 1;

        if (score >= 4) return "high";
        if (score >= 2) return "medium";
        return "low";
    }

    private Map<String, List<String>> getPlanByRiskLevel(String riskLevel) {
        Map<String, List<String>> plans = new HashMap<>();

        if ("low".equals(riskLevel)) {
            plans.put("morning", Arrays.asList(
                    "Reveil en douceur avec 5 minutes de calins",
                    "Petit-dejeuner equilibre ensemble"));
            plans.put("afternoon", Arrays.asList(
                    "Activite creative (dessin, peinture)",
                    "30 minutes de jeu en exterieur"));
            plans.put("evening", Arrays.asList(
                    "Diner en famille sans ecrans",
                    "Rituel du bain relaxant"));
            plans.put("communication", Arrays.asList(
                    "Poser des questions ouvertes",
                    "Ecouter activement"));
            plans.put("games", Arrays.asList(
                    "Jeux de society educatifs",
                    "Memory ou puzzles"));
        } else if ("medium".equals(riskLevel)) {
            plans.put("morning", Arrays.asList(
                    "Reveil progressif avec musique douce",
                    "Petit-dejeuner proteine"));
            plans.put("afternoon", Arrays.asList(
                    "Exercices de respiration (5 minutes)",
                    "Jeux de concentration"));
            plans.put("evening", Arrays.asList(
                    "Diner calme sans stimulation",
                    "Lecture calmante"));
            plans.put("communication", Arrays.asList(
                    "Utiliser des phrases courtes",
                    "Valider ses emotions"));
            plans.put("games", Arrays.asList(
                    "Jeux d'equilibre",
                    "Yoga pour enfants"));
        } else {
            plans.put("morning", Arrays.asList(
                    "Reveil anticipe pour eviter la pression",
                    "Routine visuelle avec pictogrammes"));
            plans.put("afternoon", Arrays.asList(
                    "Creer un coin calme avec coussins",
                    "Activite sensorielle"));
            plans.put("evening", Arrays.asList(
                    "Reduire les ecrans 2h avant",
                    "Rituel de coucherstructure"));
            plans.put("communication", Arrays.asList(
                    "Utiliser le toucher rassurant",
                    "Parler avec un ton calme"));
            plans.put("games", Arrays.asList(
                    "Jeux de role pour exprimer les emotions",
                    "Cartes des emotions"));
        }
        return plans;
    }

    private ActionPlan createDefaultPlan(Child child) {
        ActionPlan plan = new ActionPlan();
        plan.setChild(child);
        plan.setRiskLevel("low");
        plan.setGeneratedDate(LocalDate.now());
        plan.setMorningActivities("Pas assez de donnees pour generer un plan personnalise");
        plan.setAfternoonActivities("Continuez a enregistrer des logs quotidiens");
        plan.setEveningActivities("7 jours de logs sont necessaires pour un plan complet");
        plan.setCommunicationTips("Enregistrez plus de donnees pour des conseils personnalises");
        plan.setGamesActivities("Revenez apres quelques jours de suivi");
        return actionPlanRepository.save(plan);
    }

    public ActionPlan getLatestPlan(Long childId) {
        return actionPlanRepository.findTopByChildIdOrderByGeneratedDateDesc(childId);
    }
}