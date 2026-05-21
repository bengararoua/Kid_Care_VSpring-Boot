package com.kidcare.insight.controller;

import com.kidcare.insight.entity.*;
import com.kidcare.insight.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChildRepository childRepository;
    
    @Autowired
    private BehaviorLogRepository behaviorLogRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Map<String, Object> data = new HashMap<>();
        
        if (currentUser.isParent()) {
            List<Child> children = childRepository.findByParent(currentUser);
            long unreadMessages = messageRepository.countByReceiverAndIsReadFalse(currentUser);
            long unreadNotifications = notificationRepository.countByUserAndIsReadFalse(currentUser);
            
            // Logs récents (10 derniers)
            List<BehaviorLog> recentLogs = new ArrayList<>();
            for (Child child : children) {
                recentLogs.addAll(behaviorLogRepository.findByChildOrderByLogDateDesc(child));
            }
            recentLogs.sort((a, b) -> b.getLogDate().compareTo(a.getLogDate()));
            recentLogs = recentLogs.stream().limit(10).collect(Collectors.toList());
            
            // Statistiques
            int totalChildren = children.size();
            int totalRecommendations = 0;
            for (Child child : children) {
                totalRecommendations += recommendationRepository.findByChildAndIsCompletedFalse(child).size();
            }
            
            data.put("children", children);
            data.put("childrenCount", totalChildren);
            data.put("recentLogs", recentLogs);
            data.put("unreadMessages", unreadMessages);
            data.put("unreadNotifications", unreadNotifications);
            data.put("pendingRecommendations", totalRecommendations);
            data.put("role", "parent");
            
        } else if (currentUser.isTeacher()) {
            List<Child> children = childRepository.findByTeacher(currentUser);
            long unreadMessages = messageRepository.countByReceiverAndIsReadFalse(currentUser);
            long unreadNotifications = notificationRepository.countByUserAndIsReadFalse(currentUser);
            
            // Logs d'aujourd'hui
            LocalDate today = LocalDate.now();
            List<BehaviorLog> todayLogs = new ArrayList<>();
            for (Child child : children) {
                todayLogs.addAll(behaviorLogRepository.findByChildOrderByLogDateDesc(child).stream()
                    .filter(log -> log.getLogDate().equals(today))
                    .collect(Collectors.toList()));
            }
            
            // Enfants à risque (focus moyen < 2.5 sur les 7 derniers jours)
            List<Map<String, Object>> highRiskChildren = new ArrayList<>();
            for (Child child : children) {
                List<BehaviorLog> recentLogs = behaviorLogRepository.findByChildOrderByLogDateDesc(child).stream()
                    .limit(7)
                    .collect(Collectors.toList());
                if (!recentLogs.isEmpty()) {
                    double avgFocus = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);
                    if (avgFocus < 2.5) {
                        Map<String, Object> riskChild = new HashMap<>();
                        riskChild.put("id", child.getId());
                        riskChild.put("name", child.getName());
                        riskChild.put("age", child.getAge());
                        riskChild.put("avgFocus", Math.round(avgFocus * 10) / 10.0);
                        highRiskChildren.add(riskChild);
                    }
                }
            }
            
            data.put("children", children);
            data.put("childrenCount", children.size());
            data.put("todayLogsCount", todayLogs.size());
            data.put("highRiskChildren", highRiskChildren);
            data.put("highRiskCount", highRiskChildren.size());
            data.put("unreadMessages", unreadMessages);
            data.put("unreadNotifications", unreadNotifications);
            data.put("role", "teacher");
            
        } else if (currentUser.isPsychologist()) {
            List<Child> children = childRepository.findByPsychologist(currentUser);
            long unreadMessages = messageRepository.countByReceiverAndIsReadFalse(currentUser);
            long unreadNotifications = notificationRepository.countByUserAndIsReadFalse(currentUser);
            
            // Enfants à risque
            List<Map<String, Object>> highRiskChildren = new ArrayList<>();
            for (Child child : children) {
                List<BehaviorLog> recentLogs = behaviorLogRepository.findByChildOrderByLogDateDesc(child).stream()
                    .limit(7)
                    .collect(Collectors.toList());
                if (!recentLogs.isEmpty()) {
                    double avgFocus = recentLogs.stream().mapToInt(BehaviorLog::getFocusLevel).average().orElse(5);
                    double avgSleep = recentLogs.stream().mapToDouble(BehaviorLog::getSleepHours).average().orElse(8);
                    if (avgFocus < 2.5 || avgSleep < 6) {
                        Map<String, Object> riskChild = new HashMap<>();
                        riskChild.put("id", child.getId());
                        riskChild.put("name", child.getName());
                        riskChild.put("age", child.getAge());
                        riskChild.put("avgFocus", Math.round(avgFocus * 10) / 10.0);
                        riskChild.put("avgSleep", Math.round(avgSleep * 10) / 10.0);
                        highRiskChildren.add(riskChild);
                    }
                }
            }
            
            // Recommandations en attente
            List<Recommendation> pendingRecs = new ArrayList<>();
            for (Child child : children) {
                pendingRecs.addAll(recommendationRepository.findByChildAndIsCompletedFalse(child));
            }
            pendingRecs = pendingRecs.stream().limit(5).collect(Collectors.toList());
            
            data.put("children", children);
            data.put("childrenCount", children.size());
            data.put("highRiskChildren", highRiskChildren);
            data.put("highRiskCount", highRiskChildren.size());
            data.put("pendingRecs", pendingRecs);
            data.put("pendingRecsCount", pendingRecs.size());
            data.put("unreadMessages", unreadMessages);
            data.put("unreadNotifications", unreadNotifications);
            data.put("role", "psychologist");
        }
        
        return ResponseEntity.ok(data);
    }
}