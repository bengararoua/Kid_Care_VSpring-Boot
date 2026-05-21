package com.kidcare.insight.service;

import com.kidcare.insight.dto.BehaviorLogRequest;
import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.Notification;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.BehaviorLogRepository;
import com.kidcare.insight.repository.ChildRepository;
import com.kidcare.insight.repository.NotificationRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BehaviorService {

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public List<BehaviorLog> getLogsForChild(Long childId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Child child = childRepository.findById(childId).orElseThrow();
        return behaviorLogRepository.findByChildOrderByLogDateDesc(child);
    }

    public BehaviorLog createLog(BehaviorLogRequest req, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        Child child = childRepository.findById(req.getChildId()).orElseThrow();

        BehaviorLog log = new BehaviorLog();
        log.setChild(child);
        log.setUser(currentUser);
        log.setFocusLevel(req.getFocusLevel());
        log.setMood(req.getMood());
        log.setSleepHours(req.getSleepHours());
        log.setSocialInteraction(req.getSocialInteraction());
        log.setNote(req.getNote());
        log.setLogDate(req.getLogDate());

        BehaviorLog saved = behaviorLogRepository.save(log);

        if (child.getParent() != null && (currentUser.isTeacher() || currentUser.isPsychologist())) {
            Notification notif = new Notification();
            notif.setUser(child.getParent());
            notif.setType("behavior_alert");
            notif.setTitle("New behavior log");
            notif.setMessage("A new behavior log was added for " + child.getName());
            notif.setData("{\"childId\":" + child.getId() + "}");
            notificationRepository.save(notif);
        }

        return saved;
    }

    public void deleteLog(Long logId, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        BehaviorLog log = behaviorLogRepository.findById(logId).orElseThrow();
        if (!log.getUser().getId().equals(currentUser.getId()) && !currentUser.isParent()) {
            throw new RuntimeException("Unauthorized");
        }
        behaviorLogRepository.delete(log);
    }
}