package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.entity.Message;  // Importe l'entité représentant un message envoyé entre utilisateurs
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.MessageRepository;  // Importe le repository JPA pour accéder aux messages en base
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux utilisateurs en base
import com.kidcare.insight.service.MessageService;  // Importe le service contenant la logique métier pour les messages
import com.kidcare.insight.service.NotificationService;  // Importe le service contenant la logique d'envoi de notifications
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (GetMapping, PostMapping, PutMapping, DeleteMapping, etc.)
import java.time.LocalDateTime;  // Importe la classe pour manipuler les dates et heures
import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur
import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur
import java.util.stream.Collectors;  // Importe la classe Collectors pour collecter les résultats des streams Java

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
public class MessageController {  // Déclare le contrôleur gérant la messagerie entre utilisateurs (parents, enseignants, psychologues)

    @Autowired  // Injecte automatiquement le bean MessageService géré par Spring
    private MessageService messageService;  // Service contenant la logique métier pour les messages (envoi, lecture, suppression)

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base

    @Autowired  // Injecte automatiquement le bean MessageRepository géré par Spring
    private MessageRepository messageRepository;  // Repository pour accéder directement aux messages en base

    @Autowired  // Injecte automatiquement le bean NotificationService géré par Spring
    private NotificationService notificationService;  // Service pour envoyer des notifications en temps réel (WebSocket, email, push)

    @GetMapping("/messages")  // Associe les requêtes GET à "/api/messages" à cette méthode
    public ResponseEntity<Map<String, Object>> getMessages(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère tous les contacts et messages non lus de l'utilisateur
        User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base par son email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        List<User> contacts = messageService.getContacts(currentUser.getId());  // Récupère la liste des utilisateurs avec qui l'utilisateur a échangé des messages

        List<Map<String, Object>> contactsWithUnread = contacts.stream()  // Convertit la liste des contacts en flux (stream)
                .map(contact -> {  // Transforme chaque contact en map contenant ses infos + compteur non lus
                    Map<String, Object> contactMap = new HashMap<>();  // Crée une map pour stocker les infos du contact
                    contactMap.put("id", contact.getId());  // Ajoute l'ID du contact
                    contactMap.put("name", contact.getName());  // Ajoute le nom du contact
                    contactMap.put("email", contact.getEmail());  // Ajoute l'email du contact
                    contactMap.put("role", contact.getRole());  // Ajoute le rôle du contact (parent, teacher, psychologist)
                    contactMap.put("unread_count", messageService.getUnreadCount(currentUser.getId(), contact.getId()));  // Ajoute le nombre de messages non lus de ce contact
                    return contactMap;  // Retourne la map du contact
                }).collect(Collectors.toList());  // Collecte toutes les maps dans une liste

        long totalUnread = messageService.getTotalUnreadCount(currentUser.getId());  // Calcule le nombre total de messages non lus pour l'utilisateur

        Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
        response.put("contacts", contactsWithUnread);  // Ajoute la liste des contacts avec leurs compteurs non lus
        response.put("unreadCount", totalUnread);  // Ajoute le total des messages non lus

        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données en JSON
    }

    @GetMapping("/messages/conversation/{userId}")  // Associe les requêtes GET à "/api/messages/conversation/{userId}" à cette méthode
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable Long userId,  // Extrait l'ID du contact depuis l'URL
                                                               @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        List<Message> messages = messageService.getConversation(currentUser.getId(), userId);  // Récupère tous les messages échangés entre les deux utilisateurs

        if (messages != null && !messages.isEmpty()) {  // Vérifie si des messages existent dans la conversation
            messageService.markMessagesAsRead(currentUser.getId(), userId);  // Marque tous les messages reçus comme lus
        }

        Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
        response.put("messages", messages != null ? messages : List.of());  // Ajoute la liste des messages (ou liste vide si null)

        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les messages en JSON
    }

    @PostMapping("/messages/send/{userId}")  // Associe les requêtes POST à "/api/messages/send/{userId}" à cette méthode
    public ResponseEntity<?> sendMessage(@PathVariable Long userId,  // Extrait l'ID du destinataire depuis l'URL
                                         @RequestBody Map<String, String> request,  // Extrait le contenu du message depuis le corps JSON
                                         @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié (expéditeur)
        User sender = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'expéditeur en base
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        User receiver = userRepository.findById(userId)  // Cherche le destinataire en base par son ID
                .orElseThrow(() -> new RuntimeException("Receiver not found"));  // Lance exception si non trouvé

        String content = request.get("content");  // Extrait le contenu du message depuis la requête
        Message message = messageService.sendMessage(sender.getId(), receiver.getId(), content);  // Envoie le message (sauvegarde en base)

        if (message != null && message.getId() != null) {  // Vérifie si le message a bien été sauvegardé
            notificationService.notifyNewMessage(receiver, sender, content, message.getId());  // Envoie une notification en temps réel au destinataire
        }

        Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
        response.put("success", true);  // Indique que l'opération a réussi
        response.put("message", "Message sent successfully");  // Ajoute un message de confirmation
        response.put("data", message);  // Ajoute l'objet message complet (avec ID, date, etc.)

        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données en JSON
    }

    //  MODIFIER UN MESSAGE
    @PutMapping("/messages/{messageId}")  // Associe les requêtes PUT à "/api/messages/{messageId}" à cette méthode
    public ResponseEntity<?> updateMessage(@PathVariable Long messageId,  // Extrait l'ID du message à modifier depuis l'URL
                                           @RequestBody Map<String, String> request,  // Extrait le nouveau contenu depuis le corps JSON
                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        try {  // Bloc try-catch pour capturer les erreurs
            User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

            String newContent = request.get("content");  // Extrait le nouveau contenu du message
            Message updatedMessage = messageService.updateMessage(messageId, currentUser.getId(), newContent);  // Met à jour le message (vérifie que l'utilisateur est l'auteur)

            Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("success", true);  // Indique que l'opération a réussi
            response.put("message", "Message updated successfully");  // Ajoute un message de confirmation
            response.put("data", updatedMessage);  // Ajoute le message mis à jour

            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données en JSON
            
        } catch (Exception e) {  // Capture toute exception (non autorisé, message non trouvé)
            Map<String, String> error = new HashMap<>();  // Crée une map pour le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec l'erreur
        }
    }

    //  SUPPRIMER UN MESSAGE
    @DeleteMapping("/messages/{messageId}")  // Associe les requêtes DELETE à "/api/messages/{messageId}" à cette méthode
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId,  // Extrait l'ID du message à supprimer depuis l'URL
                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        try {  // Bloc try-catch pour capturer les erreurs
            User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

            messageService.deleteMessage(messageId, currentUser.getId());  // Supprime le message (vérifie que l'utilisateur est l'auteur)

            Map<String, String> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("success", "true");  // Indique que l'opération a réussi
            response.put("message", "Message deleted successfully");  // Ajoute un message de confirmation

            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le message de confirmation
            
        } catch (Exception e) {  // Capture toute exception
            Map<String, String> error = new HashMap<>();  // Crée une map pour le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec l'erreur
        }
    }

    //  SUPPRIMER TOUTE LA CONVERSATION
    @DeleteMapping("/messages/conversation/{userId}")  // Associe les requêtes DELETE à "/api/messages/conversation/{userId}" à cette méthode
    public ResponseEntity<?> deleteConversation(@PathVariable Long userId,  // Extrait l'ID du contact depuis l'URL
                                                @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        try {  // Bloc try-catch pour capturer les erreurs
            User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

            int deletedCount = messageService.deleteConversation(currentUser.getId(), userId);  // Supprime tous les messages échangés avec ce contact

            Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("success", true);  // Indique que l'opération a réussi
            response.put("message", "Conversation deleted successfully");  // Ajoute un message de confirmation
            response.put("deleted_count", deletedCount);  // Ajoute le nombre de messages supprimés

            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données en JSON
            
        } catch (Exception e) {  // Capture toute exception
            Map<String, String> error = new HashMap<>();  // Crée une map pour le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec l'erreur
        }
    }

    //  MARQUER TOUS LES MESSAGES COMME LUS
    @PutMapping("/messages/read-all/{userId}")  // Associe les requêtes PUT à "/api/messages/read-all/{userId}" à cette méthode
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId,  // Extrait l'ID du contact depuis l'URL
                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        try {  // Bloc try-catch pour capturer les erreurs
            User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                    .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

            int updatedCount = messageService.markAllAsRead(currentUser.getId(), userId);  // Marque tous les messages reçus de ce contact comme lus

            Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("success", true);  // Indique que l'opération a réussi
            response.put("message", "All messages marked as read");  // Ajoute un message de confirmation
            response.put("updated_count", updatedCount);  // Ajoute le nombre de messages marqués comme lus

            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les données en JSON
            
        } catch (Exception e) {  // Capture toute exception
            Map<String, String> error = new HashMap<>();  // Crée une map pour le message d'erreur
            error.put("error", e.getMessage());  // Ajoute le message d'erreur
            return ResponseEntity.badRequest().body(error);  // Retourne HTTP 400 (Bad Request) avec l'erreur
        }
    }

    @GetMapping("/messages/unread-count")  // Associe les requêtes GET à "/api/messages/unread-count" à cette méthode
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère le nombre total de messages non lus
        User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        long unreadCount = messageRepository.countByReceiverAndIsReadFalse(currentUser);  // Compte les messages où l'utilisateur est destinataire ET non lus

        Map<String, Long> response = new HashMap<>();  // Crée une map pour la réponse JSON
        response.put("unreadCount", unreadCount);  // Ajoute le nombre de messages non lus

        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec le compteur en JSON
    }

    @PostMapping("/messages/mark-read/{messageId}")  // Associe les requêtes POST à "/api/messages/mark-read/{messageId}" à cette méthode
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId,  // Extrait l'ID du message à marquer comme lu depuis l'URL
                                           @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié
        User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        messageService.markAsRead(messageId, currentUser.getId());  // Marque un message spécifique comme lu (vérifie que l'utilisateur est le destinataire)

        return ResponseEntity.ok().build();  // Retourne HTTP 200 (OK) sans body (réponse vide)
    }
}