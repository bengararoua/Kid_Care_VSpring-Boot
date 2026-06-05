package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.dto.BehaviorLogRequest;  // Importe le DTO pour la requête de création de log
import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité BehaviorLog (journal de comportement)
import com.kidcare.insight.entity.Child;  // Importe l'entité Child (enfant concerné)
import com.kidcare.insight.entity.Notification;  // Importe l'entité Notification pour envoyer des alertes
import com.kidcare.insight.entity.User;  // Importe l'entité User (utilisateur créateur)
import com.kidcare.insight.repository.BehaviorLogRepository;  // Importe le repository pour les logs
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.kidcare.insight.repository.NotificationRepository;  // Importe le repository pour les notifications
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import com.fasterxml.jackson.core.JsonProcessingException;  // Importe l'exception pour les erreurs JSON
import com.fasterxml.jackson.databind.ObjectMapper;  // Importe le mapper JSON pour sérialiser les données
import org.slf4j.Logger;  // Importe l'interface de logging SLF4J
import org.slf4j.LoggerFactory;  // Importe la fabrique pour créer des loggers
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDate;  // Importe la classe LocalDate pour la date du log
import java.util.HashMap;  // Importe la classe HashMap pour les données des notifications
import java.util.List;  // Importe l'interface List pour les collections
import java.util.Map;  // Importe l'interface Map pour les données clé-valeur

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class BehaviorService {  // Déclare le service responsable des journaux de comportement

    private static final Logger logger = LoggerFactory.getLogger(BehaviorService.class);  // Crée un logger pour tracer l'exécution

    @Autowired  // Injecte automatiquement le repository BehaviorLogRepository
    private BehaviorLogRepository behaviorLogRepository;  // Repository pour les journaux de comportement
    
    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour les enfants
    
    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour les utilisateurs
    
    @Autowired  // Injecte automatiquement le repository NotificationRepository
    private NotificationRepository notificationRepository;  // Repository pour les notifications
    
    @Autowired  // Injecte automatiquement le mapper JSON
    private ObjectMapper objectMapper;  // Mapper pour convertir Map en JSON

    // Méthode pour récupérer tous les logs d'un enfant (avec vérification des droits)
    public List<BehaviorLog> getLogsForChild(Long childId, String username) {
        User user = userRepository.findByEmail(username)  // Cherche l'utilisateur par email
            .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        Child child = childRepository.findById(childId)  // Cherche l'enfant par ID
            .orElseThrow(() -> new RuntimeException("Child not found"));  // Lance exception si non trouvé
        
        boolean isAuthorized = false;  // Initialise le flag d'autorisation à false
        
        // Vérification des droits selon le rôle de l'utilisateur
        if (user.isParent() && child.getParent() != null && child.getParent().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise si l'utilisateur est le parent de l'enfant
        }
        else if (user.isTeacher() && child.getTeacher() != null && child.getTeacher().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise si l'utilisateur est l'enseignant de l'enfant
        }
        else if (user.isPsychologist() && child.getPsychologist() != null && child.getPsychologist().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise si l'utilisateur est le psychologue de l'enfant
        }
        else if (!user.isParent() && !user.isTeacher() && !user.isPsychologist()) {
            // Nouvel utilisateur sans rôle attribué
            logger.info("New user {} has no associated children yet", username);  // Log d'information
            return List.of();  // Retourne une liste vide (pas d'erreur)
        }
        
        if (!isAuthorized) {  // Si l'utilisateur n'est pas autorisé
            logger.warn("User {} is not authorized to access child {} - returning empty list", username, childId);  // Log d'avertissement
            return List.of();  // Retourne une liste vide (silencieux)
        }
        
        // Retourne tous les logs de l'enfant triés par date décroissante
        return behaviorLogRepository.findByChildIdOrderByLogDateDesc(childId);
    }

    @Transactional  // Exécute la méthode dans une transaction (annule en cas d'erreur)
    public BehaviorLog createLog(BehaviorLogRequest req, String username) {  // Crée un nouveau journal de comportement
        User user = userRepository.findByEmail(username)  // Cherche l'utilisateur par email
            .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        Child child = childRepository.findById(req.getChildId())  // Cherche l'enfant par ID
            .orElseThrow(() -> new RuntimeException("Child not found"));  // Lance exception si non trouvé
        
        boolean isAuthorized = false;  // Initialise le flag d'autorisation
        
        // Vérification des droits pour créer un log
        if (user.isParent() && child.getParent() != null && child.getParent().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise le parent
        } else if (user.isTeacher() && child.getTeacher() != null && child.getTeacher().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise l'enseignant
        } else if (user.isPsychologist() && child.getPsychologist() != null && child.getPsychologist().getId().equals(user.getId())) {
            isAuthorized = true;  // Autorise le psychologue
        }
        
        if (!isAuthorized) {  // Si non autorisé
            throw new RuntimeException("Unauthorized - You are not authorized to create logs for this child");  // Lance exception
        }
        
        // Création et remplissage du log
        BehaviorLog behaviorLog = new BehaviorLog();  // Crée une nouvelle instance
        behaviorLog.setChild(child);  // Associe l'enfant
        behaviorLog.setUser(user);  // Associe l'utilisateur créateur
        behaviorLog.setFocusLevel(req.getFocusLevel());  // Définit le niveau de concentration
        behaviorLog.setMood(req.getMood());  // Définit l'humeur
        behaviorLog.setSleepHours(req.getSleepHours());  // Définit les heures de sommeil
        behaviorLog.setSocialInteraction(req.getSocialInteraction());  // Définit l'interaction sociale
        behaviorLog.setNote(req.getNote());  // Définit la note
        behaviorLog.setLogDate(req.getLogDate());  // Définit la date du log
        
        BehaviorLog savedLog = behaviorLogRepository.save(behaviorLog);  // Sauvegarde le log en base
        
        // Envoi des notifications aux parties prenantes
        try {
            // Notification au PARENT (si le créateur n'est pas le parent)
            if (child.getParent() != null && !child.getParent().getId().equals(user.getId())) {
                Map<String, Object> dataMap = new HashMap<>();  // Crée une map pour les données
                dataMap.put("child_id", child.getId());  // Ajoute l'ID de l'enfant
                dataMap.put("log_id", savedLog.getId());  // Ajoute l'ID du log
                String jsonData = objectMapper.writeValueAsString(dataMap);  // Convertit la map en JSON
                
                Notification parentNotification = new Notification(  // Crée une notification
                    child.getParent(),  // Destinataire : le parent
                    "new_behavior_log",  // Type de notification
                    "📝 Nouveau log comportemental",  // Titre
                    "Un nouveau log a été ajouté pour " + child.getName() + " (Focus: " + req.getFocusLevel() + "/5)",  // Message
                    jsonData  // Données JSON
                );
                notificationRepository.save(parentNotification);  // Sauvegarde la notification
            }
            
            // Notification au PSYCHOLOGUE (si le créateur n'est pas le psychologue)
            if (child.getPsychologist() != null && !child.getPsychologist().getId().equals(user.getId())) {
                Map<String, Object> dataMap = new HashMap<>();  // Crée une map pour les données
                dataMap.put("child_id", child.getId());  // Ajoute l'ID de l'enfant
                dataMap.put("log_id", savedLog.getId());  // Ajoute l'ID du log
                String jsonData = objectMapper.writeValueAsString(dataMap);  // Convertit la map en JSON
                
                Notification psychologistNotification = new Notification(  // Crée une notification
                    child.getPsychologist(),  // Destinataire : le psychologue
                    "new_behavior_log",  // Type de notification
                    "📊 Nouveau log pour " + child.getName(),  // Titre
                    "Focus: " + req.getFocusLevel() + "/5 | Sommeil: " + req.getSleepHours() + "h | Humeur: " + req.getMood(),  // Message
                    jsonData  // Données JSON
                );
                notificationRepository.save(psychologistNotification);  // Sauvegarde la notification
            }
        } catch (JsonProcessingException e) {  // En cas d'erreur de conversion JSON
            logger.error("Error creating JSON for notification: {}", e.getMessage());  // Log l'erreur
        }
        
        // Système de points pour les parents (gamification)
        if (user.isParent()) {  // Si l'utilisateur est un parent
            String today = LocalDate.now().toString();  // Récupère la date du jour
            if (user.getLastLogDate() == null || !today.equals(user.getLastLogDate().toString())) {  // Si pas de log aujourd'hui
                // Vérifier que points n'est pas null avant d'incrémenter
                if (user.getPoints() == null) {  // Si les points sont null
                    user.setPoints(0);  // Initialise à 0
                }
                user.incrementPoints(10);  // Ajoute 10 points
                user.setLastLogDate(LocalDate.now());  // Met à jour la dernière date de log
                userRepository.save(user);  // Sauvegarde l'utilisateur
                
                try {
                    Map<String, Object> pointsData = new HashMap<>();  // Crée une map pour les données
                    pointsData.put("points", 10);  // Ajoute les points gagnés
                    pointsData.put("total_points", user.getPoints() != null ? user.getPoints() : 0);  // Ajoute le total
                    String pointsJson = objectMapper.writeValueAsString(pointsData);  // Convertit en JSON
                    
                    int totalPoints = user.getPoints() != null ? user.getPoints() : 0;  // Récupère le total
                    Notification pointsNotification = new Notification(  // Crée une notification de récompense
                        user,  // Destinataire : l'utilisateur lui-même
                        "milestone",  // Type de notification
                        "🎉 +10 points !",  // Titre
                        "Vous avez gagné 10 points pour avoir enregistré le comportement de " + child.getName() + ". Total: " + totalPoints + " points",  // Message
                        pointsJson  // Données JSON
                    );
                    notificationRepository.save(pointsNotification);  // Sauvegarde la notification
                } catch (JsonProcessingException e) {  // En cas d'erreur JSON
                    logger.error("Error creating points notification JSON: {}", e.getMessage());  // Log l'erreur
                }
            }
        }
        
        return savedLog;  // Retourne le log sauvegardé
    }

    @Transactional  // Exécute la méthode dans une transaction
    public void deleteLog(Long id, String username) {  // Supprime un journal de comportement
        User user = userRepository.findByEmail(username)  // Cherche l'utilisateur par email
            .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        BehaviorLog behaviorLog = behaviorLogRepository.findById(id)  // Cherche le log par ID
            .orElseThrow(() -> new RuntimeException("Log not found"));  // Lance exception si non trouvé
        
        // Vérifie que l'utilisateur est le créateur OU un parent (qui peut supprimer)
        if (!behaviorLog.getUser().getId().equals(user.getId()) && !user.isParent()) {
            throw new RuntimeException("Unauthorized to delete this log");  // Lance exception si non autorisé
        }
        
        behaviorLogRepository.delete(behaviorLog);  // Supprime le log
    }
}