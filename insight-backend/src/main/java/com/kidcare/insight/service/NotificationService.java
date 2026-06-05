package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour les notifications liées aux enfants
import com.kidcare.insight.entity.Notification;  // Importe l'entité Notification pour créer et gérer les notifications
import com.kidcare.insight.entity.User;  // Importe l'entité User pour les destinataires des notifications
import com.kidcare.insight.repository.NotificationRepository;  // Importe le repository pour les notifications
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions

import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour les horodatages
import java.util.HashMap;  // Importe la classe HashMap pour les données des notifications
import java.util.List;  // Importe l'interface List pour les collections
import java.util.Map;  // Importe l'interface Map pour les données clé-valeur

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class NotificationService {  // Déclare le service responsable de la gestion des notifications

    @Autowired  // Injecte automatiquement le repository NotificationRepository
    private NotificationRepository notificationRepository;  // Repository pour sauvegarder et récupérer les notifications

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer les utilisateurs

    // ===== MÉTHODES DE LECTURE =====

    // Méthode pour récupérer toutes les notifications d'un utilisateur (triées du plus récent au plus ancien)
    public List<Notification> getNotificationsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur par email
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);  // Retourne les notifications triées
    }

    // Méthode pour compter le nombre de notifications non lues d'un utilisateur
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur par email
        return notificationRepository.countByUserAndIsReadFalse(user);  // Retourne le compteur de non lues
    }

    // ===== MÉTHODES DE MODIFICATION =====

    @Transactional  // Exécute dans une transaction
    public void markAsRead(Long notificationId, String email) {  // Marque une notification spécifique comme lue
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();  // Récupère la notification
        if (notif.getUser().getId().equals(user.getId())) {  // Vérifie que la notification appartient à l'utilisateur
            notif.setIsRead(true);  // Marque comme lue
            notificationRepository.save(notif);  // Sauvegarde
        }
    }

    @Transactional
    public void markAllAsRead(String email) {  // Marque toutes les notifications d'un utilisateur comme lues
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur
        notificationRepository.markAllAsRead(user);  // Appelle la requête de mise à jour en masse
    }

    @Transactional
    public void deleteNotification(Long notificationId, String email) {  // Supprime une notification spécifique
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();  // Récupère la notification
        if (notif.getUser().getId().equals(user.getId())) {  // Vérifie que la notification appartient à l'utilisateur
            notificationRepository.delete(notif);  // Supprime la notification
        }
    }

    @Transactional
    public void deleteAllNotifications(String email) {  // Supprime toutes les notifications d'un utilisateur
        User user = userRepository.findByEmail(email).orElseThrow();  // Récupère l'utilisateur
        notificationRepository.deleteByUser(user);  // Suppression en masse
    }

    // ===== MÉTHODES DE CRÉATION DE NOTIFICATIONS =====

    @Transactional
    public Notification createNotification(User user, String type, String title, String message, Map<String, Object> data) {
        // Crée une notification avec toutes les informations (utilisateur, type, titre, message, données JSON)
        Notification notification = new Notification(user, type, title, message, data);  // Utilise le constructeur avec Map
        return notificationRepository.save(notification);  // Sauvegarde et retourne la notification
    }

    @Transactional
    public Notification createSimpleNotification(User user, String type, String title, String message) {
        // Version simplifiée (sans données supplémentaires)
        return createNotification(user, type, title, message, null);  // Appelle la méthode complète avec null
    }

    // ===== MÉTHODE PRIVÉE POUR NOTIFIER TOUTES LES PARTIES PRENANTES =====

    // Notifie le parent, l'enseignant et le psychologue d'un enfant (sauf celui qui est exclu)
    private void notifyAllStakeholders(Child child, User excludedUser,
                                       String type, String title, String message,
                                       Map<String, Object> data) {
        User parent = child.getParent();  // Récupère le parent
        User teacher = child.getTeacher();  // Récupère l'enseignant
        User psychologist = child.getPsychologist();  // Récupère le psychologue

        // Notifie le parent (sauf si c'est l'utilisateur exclu)
        if (parent != null && !parent.getId().equals(excludedUser != null ? excludedUser.getId() : null)) {
            createNotification(parent, type, title, message, data);
        }
        // Notifie l'enseignant (sauf si c'est l'utilisateur exclu)
        if (teacher != null && !teacher.getId().equals(excludedUser != null ? excludedUser.getId() : null)) {
            createNotification(teacher, type, title, message, data);
        }
        // Notifie le psychologue (sauf si c'est l'utilisateur exclu)
        if (psychologist != null && !psychologist.getId().equals(excludedUser != null ? excludedUser.getId() : null)) {
            createNotification(psychologist, type, title, message, data);
        }
    }

    // ===== NOTIFICATIONS SPÉCIFIQUES =====

    @Transactional
    public void notifyChildAdded(User creator, String childName, Long childId) {  // Notification : enfant ajouté
        Map<String, Object> data = new HashMap<>();  // Crée une map pour les données
        data.put("childId", childId);  // Ajoute l'ID de l'enfant
        data.put("childName", childName);  // Ajoute le nom de l'enfant
        createNotification(creator, "system", "Enfant ajouté",  // Crée la notification
            "L'enfant " + childName + " a été ajouté à votre suivi.", data);
    }

    @Transactional
    public void notifyProfessionalAssigned(User professional, String childName, Long childId) {  // Notification : professionnel assigné
        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("childName", childName);
        String role = professional.getRole().toLowerCase();  // Récupère le rôle (teacher/psychologist)
        String roleLabel = role.equals("teacher") ? "enseignant(e)" : "psychologue";  // Libellé français
        createNotification(professional, "system", "Nouvel enfant assigné",
            "Vous avez été assigné(e) comme " + roleLabel + " pour l'enfant " + childName + ".", data);
    }

    @Transactional
    public void notifyNewMessage(User receiver, User sender, String messagePreview, Long messageId) {  // Notification : nouveau message
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", sender.getId());
        data.put("senderName", sender.getName());
        data.put("messageId", messageId);
        // Tronque l'aperçu du message à 50 caractères maximum
        data.put("messagePreview", messagePreview.length() > 50 ? messagePreview.substring(0, 50) + "..." : messagePreview);
        createNotification(receiver, "message", "Nouveau message de " + sender.getName(), messagePreview, data);
    }

    @Transactional
    public void notifyRoutineReminder(Child child, String routineName, Long routineId) {  // Notification : rappel de routine
        if (child.getParent() == null) return;  // Si pas de parent, on ne notifie personne
        Map<String, Object> data = new HashMap<>();
        data.put("routineId", routineId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("routineName", routineName);
        createNotification(child.getParent(), "reminder", "Rappel de routine",
            "Il est temps de compléter la routine « " + routineName + " » pour " + child.getName() + ".", data);
    }

    @Transactional
    public void notifyRecommendation(Child child, User author, String recommendationTitle, Long recommendationId) {  // Notification : nouvelle recommandation
        Map<String, Object> data = new HashMap<>();
        data.put("recommendationId", recommendationId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("recommendationTitle", recommendationTitle);
        String message = "Nouvelle recommandation pour " + child.getName() + " : " + recommendationTitle;
        // Notifie le parent (sauf si c'est l'auteur)
        if (child.getParent() != null && !child.getParent().getId().equals(author.getId())) {
            createNotification(child.getParent(), "recommendation", "Nouvelle recommandation", message, data);
        }
        // Notifie l'enseignant (sauf si c'est l'auteur)
        if (child.getTeacher() != null && !child.getTeacher().getId().equals(author.getId())) {
            createNotification(child.getTeacher(), "recommendation", "Nouvelle recommandation", message, data);
        }
    }

    @Transactional
    public void notifyNoteAdded(Child child, User author, String notePreview, Long noteId) {  // Notification : note psychologique ajoutée
        if (child.getParent() == null || child.getParent().getId().equals(author.getId())) return;  // Pas de parent ou auteur = parent
        Map<String, Object> data = new HashMap<>();
        data.put("noteId", noteId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("notePreview", notePreview.length() > 60 ? notePreview.substring(0, 60) + "..." : notePreview);
        createNotification(child.getParent(), "note_added", "📝 Nouvelle note psychologique",
            "Le psychologue " + author.getName() + " a ajouté une note pour " + child.getName() + ".", data);
    }

    @Transactional
    public void notifyRecommendationAdded(Child child, User author, String recommendationTitle, Long recommendationId) {  // Notification : recommandation ajoutée par psychologue
        if (child.getParent() == null || child.getParent().getId().equals(author.getId())) return;
        Map<String, Object> data = new HashMap<>();
        data.put("recommendationId", recommendationId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("recommendationTitle", recommendationTitle);
        createNotification(child.getParent(), "recommendation_added", "🎯 Nouvelle recommandation",
            "Le psychologue " + author.getName() + " a ajouté une recommandation pour " + child.getName() + " : " + recommendationTitle, data);
    }

    @Transactional
    public void notifyRecommendationCompleted(Child child, User completedBy, String recommendationTitle, Long recommendationId) {  // Notification : recommandation complétée
        if (child.getPsychologist() == null) return;  // Pas de psychologue assigné
        Map<String, Object> data = new HashMap<>();
        data.put("recommendationId", recommendationId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("recommendationTitle", recommendationTitle);
        String roleLabel = completedBy.isParent() ? "parent" : "enseignant";  // Détermine le rôle de celui qui a complété
        createNotification(child.getPsychologist(), "recommendation_completed", "✅ Recommandation complétée",
            "Le " + roleLabel + " " + completedBy.getName() + " a complété la recommandation : " + recommendationTitle + " pour " + child.getName(), data);
    }

    @Transactional
    public void notifyBehaviorAlert(Child child, User author, String behaviorType, Long logId) {  // Notification : alerte comportementale
        Map<String, Object> data = new HashMap<>();
        data.put("logId", logId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("behaviorType", behaviorType);
        // Notifie toutes les parties prenantes (sauf l'auteur)
        notifyAllStakeholders(child, author, "behavior_alert", "Alerte comportementale",
            "Comportement « " + behaviorType + " » observé chez " + child.getName() + ".", data);
    }

    @Transactional
    public void notifyNewBehaviorLog(Child child, User author, String behaviorType, Long logId) {  // Notification : nouveau log comportemental
        Map<String, Object> data = new HashMap<>();
        data.put("logId", logId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("behaviorType", behaviorType);
        notifyAllStakeholders(child, author, "new_behavior_log", "Nouvelle observation",
            "Une nouvelle observation a été enregistrée pour " + child.getName() + ".", data);
    }

    @Transactional
    public void notifyAppointmentCreated(User receiver, User sender, LocalDateTime dateTime, Long appointmentId) {  // Notification : rendez-vous créé
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", appointmentId);
        data.put("withUserId", sender.getId());
        data.put("withUserName", sender.getName());
        data.put("dateTime", dateTime.toString());
        String formattedDate = dateTime.toLocalDate().toString();
        String formattedTime = dateTime.toLocalTime().toString();
        
        // Notification pour le destinataire
        createNotification(receiver, "system", "Nouveau rendez-vous",
            "Rendez-vous avec " + sender.getName() + " le " + formattedDate + " à " + formattedTime + ".", data);
        
        // Notification pour l'expéditeur (confirmation)
        Map<String, Object> senderData = new HashMap<>(data);
        senderData.put("withUserId", receiver.getId());
        senderData.put("withUserName", receiver.getName());
        createNotification(sender, "system", "Rendez-vous confirmé",
            "Votre rendez-vous avec " + receiver.getName() + " le " + formattedDate + " à " + formattedTime + " a été créé.", senderData);
    }

    @Transactional
    public void notifyAppointmentReminder(User user, String withUserName, LocalDateTime dateTime, Long appointmentId) {  // Notification : rappel de rendez-vous
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", appointmentId);
        data.put("withUserName", withUserName);
        data.put("dateTime", dateTime.toString());
        String formattedTime = dateTime.toLocalTime().toString();
        createNotification(user, "reminder", "Rappel de rendez-vous",
            "Vous avez un rendez-vous avec " + withUserName + " aujourd'hui à " + formattedTime + ".", data);
    }

    @Transactional
    public void notifyMilestone(Child child, String milestoneName, Long milestoneId) {  // Notification : jalon / progression
        Map<String, Object> data = new HashMap<>();
        data.put("milestoneId", milestoneId);
        data.put("childId", child.getId());
        data.put("childName", child.getName());
        data.put("milestoneName", milestoneName);
        // Notifie toutes les parties prenantes
        notifyAllStakeholders(child, null, "milestone", "Progrès réalisé 🎉",
            child.getName() + " a atteint un nouveau jalon : " + milestoneName + ".", data);
    }
}