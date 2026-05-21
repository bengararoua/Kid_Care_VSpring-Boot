package com.kidcare.insight.controller;

import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.Recommendation;
import com.kidcare.insight.entity.Routine;
import com.kidcare.insight.entity.ActionPlan;
import com.kidcare.insight.repository.BehaviorLogRepository;
import com.kidcare.insight.repository.ChildRepository;
import com.kidcare.insight.repository.RecommendationRepository;
import com.kidcare.insight.repository.RoutineRepository;
import com.kidcare.insight.repository.ActionPlanRepository;
import com.kidcare.insight.repository.UserRepository;
import com.kidcare.insight.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ActionPlanRepository actionPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/child/{childId}")
    public ResponseEntity<String> exportChildReport(@PathVariable Long childId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            Child child = childRepository.findById(childId).orElseThrow();

            // Vérifier les permissions
            if (currentUser.isParent() && (child.getParent() == null || !child.getParent().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Unauthorized");
            }
            if (currentUser.isTeacher() && (child.getTeacher() == null || !child.getTeacher().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Unauthorized");
            }
            if (currentUser.isPsychologist() && (child.getPsychologist() == null || !child.getPsychologist().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Unauthorized");
            }

            // Récupérer les données
            List<BehaviorLog> logs = behaviorLogRepository.findByChildOrderByLogDateDesc(child);
            List<Recommendation> recommendations = recommendationRepository.findByChildOrderByCreatedAtDesc(child);
            List<Routine> routines = routineRepository.findByChildOrderByDayOfWeekAscTimeAsc(child);
            ActionPlan actionPlan = actionPlanRepository.findTopByChildIdOrderByGeneratedDateDesc(childId);

            // Générer le contenu HTML du rapport
            String htmlReport = generateHtmlReport(child, logs, recommendations, routines, actionPlan, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDispositionFormData("attachment", "rapport_" + child.getName() + "_" + childId + ".html");

            return ResponseEntity.ok().headers(headers).body(htmlReport);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error generating report: " + e.getMessage());
        }
    }

    private String generateHtmlReport(Child child, List<BehaviorLog> logs, List<Recommendation> recommendations,
                                      List<Routine> routines, ActionPlan actionPlan, User currentUser) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Calcul des statistiques
        double avgFocus = logs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);
        double avgSleep = logs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(0);
        double avgSocial = logs.stream().mapToInt(BehaviorLog::getSocialInteraction).average().orElse(0);

        // Calcul du niveau de risque
        int score = 0;
        if (avgFocus < 2.5) score += 2;
        else if (avgFocus < 3.5) score += 1;
        if (avgSleep < 6) score += 2;
        else if (avgSleep < 7) score += 1;
        if (avgSocial < 2.5) score += 2;
        else if (avgSocial < 3.5) score += 1;
        String riskLevel = score >= 4 ? "high" : score >= 2 ? "medium" : "low";

        String riskColor = riskLevel.equals("high") ? "#ef4444" : riskLevel.equals("medium") ? "#f59e0b" : "#10b981";
        String riskText = riskLevel.equals("high") ? "Élevé" : riskLevel.equals("medium") ? "Modéré" : "Faible";

        // Génération du HTML
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='fr'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Rapport - ").append(child.getName()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f7fb; }");
        html.append(".report-container { max-width: 1000px; margin: 0 auto; background: white; border-radius: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; }");
        html.append(".header p { margin: 10px 0 0; opacity: 0.9; }");
        html.append(".content { padding: 30px; }");
        html.append(".section { margin-bottom: 30px; border-bottom: 1px solid #e0e6ed; padding-bottom: 20px; }");
        html.append(".section-title { font-size: 20px; font-weight: 600; color: #1a0a2e; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }");
        html.append(".stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }");
        html.append(".stat-card { background: #f9fafb; border-radius: 16px; padding: 20px; text-align: center; }");
        html.append(".stat-value { font-size: 32px; font-weight: 700; color: #1f2937; }");
        html.append(".stat-label { font-size: 12px; color: #6b7280; margin-top: 5px; }");
        html.append(".risk-badge { display: inline-block; padding: 6px 16px; border-radius: 20px; color: white; font-weight: 600; font-size: 14px; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #f3f4f6; }");
        html.append("th { background: #f9fafb; font-weight: 600; color: #374151; }");
        html.append(".rec-card { background: #fef3c7; border-radius: 12px; padding: 12px 16px; margin-bottom: 10px; }");
        html.append(".rec-title { font-weight: 600; color: #92400e; }");
        html.append(".rec-desc { font-size: 13px; color: #b45309; margin-top: 5px; }");
        html.append(".footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='report-container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>👶 Rapport de suivi - ").append(child.getName()).append("</h1>");
        html.append("<p>Généré le ").append(LocalDate.now().format(dateFormatter)).append("</p>");
        html.append("</div>");

        html.append("<div class='content'>");

        // Statistiques
        html.append("<div class='stats-grid'>");
        html.append("<div class='stat-card'><div class='stat-value'>").append(String.format("%.1f", avgFocus)).append("/5</div><div class='stat-label'>Moyenne Focus</div></div>");
        html.append("<div class='stat-card'><div class='stat-value'>").append(String.format("%.1f", avgSleep)).append("h</div><div class='stat-label'>Moyenne Sommeil</div></div>");
        html.append("<div class='stat-card'><div class='stat-value'>").append(String.format("%.1f", avgSocial)).append("/5</div><div class='stat-label'>Moyenne Social</div></div>");
        html.append("<div class='stat-card'><div class='stat-value' style='color: ").append(riskColor).append("'>").append(riskText).append("</div><div class='stat-label'>Niveau de risque</div></div>");
        html.append("</div>");

        // Logs récents
        if (!logs.isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>📋 Derniers logs</div>");
            html.append("<table>");
            html.append("<thead><tr><th>Date</th><th>Focus</th><th>Humeur</th><th>Sommeil</th><th>Social</th><th>Notes</th></tr></thead>");
            html.append("<tbody>");
            for (BehaviorLog log : logs.stream().limit(10).collect(Collectors.toList())) {
                html.append("<tr>");
                html.append("<td>").append(log.getLogDate().format(dateFormatter)).append("</td>");
                html.append("<td>").append(log.getFocusLevel()).append("/5</td>");
                html.append("<td>").append(getMoodEmoji(log.getMood())).append(" ").append(log.getMood()).append("</td>");
                html.append("<td>").append(log.getSleepHours()).append("h</td>");
                html.append("<td>").append(log.getSocialInteraction()).append("/5</td>");
                html.append("<td>").append(log.getNote() != null ? log.getNote() : "").append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }

        // Recommandations
        if (!recommendations.isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>🎯 Recommandations</div>");
            for (Recommendation rec : recommendations) {
                html.append("<div class='rec-card'>");
                html.append("<div class='rec-title'>").append(rec.getTitle()).append("</div>");
                html.append("<div class='rec-desc'>").append(rec.getDescription()).append("</div>");
                html.append("</div>");
            }
            html.append("</div>");
        }

        // Plan d'action
        if (actionPlan != null) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>📋 Plan d'action</div>");
            html.append("<p><strong>Niveau de risque:</strong> <span class='risk-badge' style='background: ").append(riskColor).append("'>").append(riskText).append("</span></p>");
            html.append("</div>");
        }

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>KidCare Insight - Rapport généré automatiquement</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String getMoodEmoji(String mood) {
        Map<String, String> emojis = Map.of(
                "happy", "😊", "sad", "😢", "angry", "😡",
                "neutral", "😐", "excited", "🤩", "anxious", "😰"
        );
        return emojis.getOrDefault(mood, "😊");
    }
}