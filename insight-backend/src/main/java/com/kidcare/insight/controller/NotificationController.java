package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.entity.Notification;  // Importe l'entité représentant une notification envoyée à un utilisateur
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.NotificationRepository;  // Importe le repository JPA pour accéder aux notifications en base
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux utilisateurs en base
import com.kidcare.insight.service.NotificationService;  // Importe le service contenant la logique métier pour les notifications
import com.fasterxml.jackson.databind.ObjectMapper;  // Importe le mapper JSON pour convertir les données en JSON
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (GetMapping, PutMapping, DeleteMapping, PostMapping, etc.)

import java.util.*;  // Importe toutes les collections Java (List, ArrayList, HashMap, Map, Optional, etc.)

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class NotificationController {  // Déclare le contrôleur gérant les notifications des utilisateurs

    @Autowired  // Injecte automatiquement le bean NotificationRepository géré par Spring
    private NotificationRepository notificationRepository;  // Repository pour accéder aux notifications en base de données

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux utilisateurs en base de données

    @Autowired  // Injecte automatiquement le bean NotificationService géré par Spring
    private NotificationService notificationService;  // Service contenant la logique métier pour les notifications

    private final ObjectMapper objectMapper = new ObjectMapper();  // Crée un mapper JSON pour convertir les données en JSON

    @GetMapping(value = "/notifications", produces = "application/json")  // Associe les requêtes GET à "/api/notifications" à cette méthode (produit du JSON)
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère toutes les notifications de l'utilisateur connecté
        Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            if (userDetails == null) {  // Vérifie si aucun utilisateur n'est authentifié
                response.put("notifications", new ArrayList<>());  // Ajoute une liste vide de notifications
                response.put("unreadCount", 0);  // Ajoute un compteur de non lus à 0
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données par défaut
            }

            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base par son email (ou null si non trouvé)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("notifications", new ArrayList<>());  // Ajoute une liste vide de notifications
                response.put("unreadCount", 0);  // Ajoute un compteur de non lus à 0
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données par défaut
            }

            List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);  // Récupère toutes les notifications de l'utilisateur (triées de la plus récente à la plus ancienne)
            long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);  // Compte le nombre de notifications non lues pour cet utilisateur

            response.put("notifications", notifications);  // Ajoute la liste des notifications dans la réponse
            response.put("unreadCount", unreadCount);  // Ajoute le compteur de non lus dans la réponse

            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les notifications en JSON
            
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            response.put("notifications", new ArrayList<>());  // Ajoute une liste vide de notifications (fallback)
            response.put("unreadCount", 0);  // Ajoute un compteur à 0 (fallback)
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données par défaut (pas d'erreur serveur)
        }
    }

    @PutMapping("/notifications/{id}/read")  // Associe les requêtes PUT à "/api/notifications/{id}/read" à cette méthode
    public ResponseEntity<Map<String, Boolean>> markAsRead(@PathVariable Long id,  // Extrait l'ID de la notification depuis l'URL
                                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        Map<String, Boolean> response = new HashMap<>();  // Crée une map pour la réponse JSON (avec booléen)

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base (ou null)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            Optional<Notification> optNotif = notificationRepository.findById(id);  // Cherche la notification par son ID
            if (optNotif.isEmpty()) {  // Vérifie si la notification n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            Notification notification = optNotif.get();  // Récupère la notification depuis l'Optional
            if (!notification.getUser().getId().equals(user.getId())) {  // Vérifie si la notification n'appartient pas à l'utilisateur connecté
                response.put("success", false);  // Indique que l'opération a échoué (non autorisé)
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            notification.setIsRead(true);  // Marque la notification comme lue
            notificationRepository.save(notification);  // Sauvegarde la modification en base de données

            response.put("success", true);  // Indique que l'opération a réussi
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec true
            
        } catch (Exception e) {  // Capture toute exception
            response.put("success", false);  // Indique que l'opération a échoué
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
        }
    }

    @PutMapping("/notifications/read-all")  // Associe les requêtes PUT à "/api/notifications/read-all" à cette méthode
    public ResponseEntity<Map<String, Boolean>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {  // Marque toutes les notifications comme lues
        Map<String, Boolean> response = new HashMap<>();  // Crée une map pour la réponse JSON (avec booléen)

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base (ou null)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            notificationRepository.markAllAsRead(user);  // Appelle une méthode personnalisée du repository pour marquer toutes les notifications de l'utilisateur comme lues
            response.put("success", true);  // Indique que l'opération a réussi
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec true
            
        } catch (Exception e) {  // Capture toute exception
            response.put("success", false);  // Indique que l'opération a échoué
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
        }
    }

    @DeleteMapping("/notifications/{id}")  // Associe les requêtes DELETE à "/api/notifications/{id}" à cette méthode
    public ResponseEntity<Map<String, Boolean>> deleteNotification(@PathVariable Long id,  // Extrait l'ID de la notification depuis l'URL
                                                                   @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        Map<String, Boolean> response = new HashMap<>();  // Crée une map pour la réponse JSON (avec booléen)

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base (ou null)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            Optional<Notification> optNotif = notificationRepository.findById(id);  // Cherche la notification par son ID
            if (optNotif.isEmpty()) {  // Vérifie si la notification n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            Notification notification = optNotif.get();  // Récupère la notification depuis l'Optional
            if (!notification.getUser().getId().equals(user.getId())) {  // Vérifie si la notification n'appartient pas à l'utilisateur connecté
                response.put("success", false);  // Indique que l'opération a échoué (non autorisé)
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            notificationRepository.delete(notification);  // Supprime la notification de la base de données
            response.put("success", true);  // Indique que l'opération a réussi
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec true
            
        } catch (Exception e) {  // Capture toute exception
            response.put("success", false);  // Indique que l'opération a échoué
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
        }
    }

    @DeleteMapping("/notifications")  // Associe les requêtes DELETE à "/api/notifications" à cette méthode
    public ResponseEntity<Map<String, Boolean>> deleteAllNotifications(@AuthenticationPrincipal UserDetails userDetails) {  // Supprime toutes les notifications de l'utilisateur
        Map<String, Boolean> response = new HashMap<>();  // Crée une map pour la réponse JSON (avec booléen)

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base (ou null)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("success", false);  // Indique que l'opération a échoué
                return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
            }

            notificationRepository.deleteByUser(user);  // Supprime toutes les notifications de l'utilisateur (méthode personnalisée)
            response.put("success", true);  // Indique que l'opération a réussi
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec true
            
        } catch (Exception e) {  // Capture toute exception
            response.put("success", false);  // Indique que l'opération a échoué
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec false
        }
    }

    @PostMapping("/notifications/test")  // Associe les requêtes POST à "/api/notifications/test" à cette méthode
    public ResponseEntity<Map<String, Object>> createTestNotification(@AuthenticationPrincipal UserDetails userDetails) {  // Crée une notification de test (endpoint de développement)
        Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);  // Cherche l'utilisateur en base (ou null)
            if (user == null) {  // Vérifie si l'utilisateur n'existe pas
                response.put("error", "User not found");  // Ajoute un message d'erreur
                return ResponseEntity.badRequest().body(response);  // Retourne HTTP 400 (Bad Request) avec l'erreur
            }

            Map<String, Object> data = new HashMap<>();  // Crée une map pour les données supplémentaires de la notification
            data.put("test", true);  // Ajoute un flag indiquant que c'est un test
            data.put("timestamp", System.currentTimeMillis());  // Ajoute l'horodatage actuel

            Notification notification = new Notification();  // Crée une nouvelle instance de notification
            notification.setUser(user);  // Associe la notification à l'utilisateur
            notification.setType("system");  // Définit le type de notification ("system")
            notification.setTitle("Test de notification");  // Définit le titre de la notification
            notification.setMessage("Bravo ! Les notifications fonctionnent correctement !");  // Définit le message de la notification
            notification.setData(objectMapper.writeValueAsString(data));  // Convertit les données supplémentaires en JSON et les sauvegarde
            notification.setIsRead(false);  // Marque la notification comme non lue

            notificationRepository.save(notification);  // Sauvegarde la notification en base de données

            response.put("success", true);  // Indique que l'opération a réussi
            response.put("message", "Notification de test creee avec succes");  // Ajoute un message de confirmation
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec la confirmation
            
        } catch (Exception e) {  // Capture toute exception
            response.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.badRequest().body(response);  // Retourne HTTP 400 (Bad Request) avec l'erreur
        }
    }
}