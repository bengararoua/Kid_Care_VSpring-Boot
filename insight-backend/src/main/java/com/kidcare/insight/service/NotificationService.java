package com.kidcare.insight.service;

import com.kidcare.insight.entity.Notification;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.NotificationRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getNotificationsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();
        if (notif.getUser().getId().equals(user.getId())) {
            notif.setIsRead(true);
            notificationRepository.save(notif);
        }
    }

    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Notification> notifs = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : notifs) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(notifs);
    }

    public void deleteNotification(Long notificationId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();
        if (notif.getUser().getId().equals(user.getId())) {
            notificationRepository.delete(notif);
        }
    }

    public void deleteAllNotifications(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Notification> notifs = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(notifs);
    }
}