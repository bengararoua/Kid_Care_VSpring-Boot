package com.kidcare.insight.service;

import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.repository.BehaviorLogRepository;
import com.kidcare.insight.repository.ChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightService {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    public Map<String, Object> getInsights(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow();
        List<BehaviorLog> logs = behaviorLogRepository.findByChildOrderByLogDateDesc(child);
        
        Map<String, Object> result = new HashMap<>();
        result.put("child_name", child.getName());
        
        if (logs.isEmpty()) {
            result.put("risk_level", "insufficient_data");
            result.put("total_logs", 0);
            result.put("message", "Not enough data");
            result.put("chart_data", new ArrayList<>());
            result.put("mood_distribution", new HashMap<>());
            result.put("pattern", new ArrayList<>());
            return result;
        }
        
        // Calcul des moyennes
        double avgFocus = logs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);
        double avgSleep = logs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(0);
        double avgSocial = logs.stream().mapToInt(BehaviorLog::getSocialInteraction).average().orElse(0);
        
        result.put("avg_focus", Math.round(avgFocus * 10) / 10.0);
        result.put("avg_sleep", Math.round(avgSleep * 10) / 10.0);
        result.put("avg_social", Math.round(avgSocial * 10) / 10.0);
        result.put("total_logs", logs.size());
        
        // Calcul du risque
        String riskLevel = calculateRiskLevel(avgFocus, avgSleep, avgSocial);
        result.put("risk_level", riskLevel);
        
        // Détection des patterns
        List<String> patterns = detectPatterns(logs);
        result.put("pattern", patterns);
        
        // Comparaison hebdomadaire
        Map<String, Object> weeklyComp = getWeeklyComparison(logs);
        result.put("weekly_comparison", weeklyComp);
        
        // Distribution des humeurs
        Map<String, Integer> moodDistribution = new HashMap<>();
        for (BehaviorLog log : logs) {
            moodDistribution.put(log.getMood(), moodDistribution.getOrDefault(log.getMood(), 0) + 1);
        }
        result.put("mood_distribution", moodDistribution);
        
        // Données pour le graphique (30 derniers jours)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<BehaviorLog> recentLogs = logs.stream()
            .filter(log -> log.getLogDate().isAfter(thirtyDaysAgo))
            .sorted((a, b) -> a.getLogDate().compareTo(b.getLogDate()))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (BehaviorLog log : recentLogs) {
            Map<String, Object> point = new HashMap<>();
            point.put("log_date", log.getLogDate().toString());
            point.put("focus_level", log.getFocusLevel());
            point.put("sleep_hours", log.getSleepHours());
            point.put("mood", log.getMood());
            point.put("social_interaction", log.getSocialInteraction());
            chartData.add(point);
        }
        result.put("chart_data", chartData);
        
        return result;
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

    private List<String> detectPatterns(List<BehaviorLog> logs) {
        List<String> patterns = new ArrayList<>();
        
        // Corrélation sommeil/focus
        List<BehaviorLog> lowSleepLogs = logs.stream()
            .filter(l -> l.getSleepHours() < 7)
            .collect(Collectors.toList());
        
        if (!lowSleepLogs.isEmpty()) {
            double avgFocusLowSleep = lowSleepLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);
            List<BehaviorLog> normalSleepLogs = logs.stream()
                .filter(l -> l.getSleepHours() >= 7)
                .collect(Collectors.toList());
            
            if (!normalSleepLogs.isEmpty()) {
                double avgFocusNormalSleep = normalSleepLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);
                if (avgFocusLowSleep < avgFocusNormalSleep - 0.5) {
                    patterns.add("Low sleep correlates with lower focus levels");
                }
            }
        }
        
        // Évolution récente
        if (logs.size() >= 14) {
            List<BehaviorLog> recentLogs = logs.stream().limit(7).collect(Collectors.toList());
            List<BehaviorLog> olderLogs = logs.stream().skip(7).limit(7).collect(Collectors.toList());
            
            double recentAvg = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);
            double olderAvg = olderLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(0);
            
            if (recentAvg < olderAvg - 0.5) {
                patterns.add("Focus level has decreased compared to previous period");
            } else if (recentAvg > olderAvg + 0.5) {
                patterns.add("Focus level is improving!");
            }
        }
        
        return patterns;
    }

    private Map<String, Object> getWeeklyComparison(List<BehaviorLog> logs) {
        LocalDate now = LocalDate.now();
        double currentWeekAvg = logs.stream()
            .filter(l -> l.getLogDate().isAfter(now.minusDays(7)))
            .mapToInt(BehaviorLog::getFocusLevel)
            .average().orElse(0);
        double previousWeekAvg = logs.stream()
            .filter(l -> l.getLogDate().isAfter(now.minusDays(14)) && l.getLogDate().isBefore(now.minusDays(7)))
            .mapToInt(BehaviorLog::getFocusLevel)
            .average().orElse(0);
        
        Map<String, Object> comp = new HashMap<>();
        comp.put("current_week_focus", Math.round(currentWeekAvg * 10) / 10.0);
        comp.put("previous_week_focus", Math.round(previousWeekAvg * 10) / 10.0);
        comp.put("change", Math.round((currentWeekAvg - previousWeekAvg) * 10) / 10.0);
        return comp;
    }
}