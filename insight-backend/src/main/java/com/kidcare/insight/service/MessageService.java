package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.Message;  // Importe l'entité Message pour la gestion des messages
import com.kidcare.insight.entity.User;  // Importe l'entité User pour les expéditeurs et destinataires
import com.kidcare.insight.repository.MessageRepository;  // Importe le repository pour les messages
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour les horodatages
import java.util.List;  // Importe l'interface List pour les collections
import java.util.stream.Collectors;  // Importe la classe Collectors pour les streams

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class MessageService {  // Déclare le service responsable de la messagerie entre utilisateurs

    @Autowired  // Injecte automatiquement le repository MessageRepository
    private MessageRepository messageRepository;  // Repository pour sauvegarder et récupérer les messages

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer les utilisateurs

    // Méthode pour récupérer tous les contacts d'un utilisateur (tous les autres utilisateurs)
    public List<User> getContacts(Long userId) {
        return userRepository.findAll().stream()  // Récupère tous les utilisateurs et les transforme en flux
                .filter(user -> !user.getId().equals(userId))  // Filtre pour exclure l'utilisateur lui-même
                .collect(Collectors.toList());  // Collecte le résultat dans une liste
    }

    // Méthode pour récupérer la conversation complète entre deux utilisateurs (triée par date)
    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);  // Appelle la requête JPQL personnalisée
    }

    @Transactional  // Exécute dans une transaction (annule en cas d'erreur)
    public Message sendMessage(Long senderId, Long receiverId, String content) {  // Envoie un nouveau message
        User sender = userRepository.findById(senderId)  // Cherche l'expéditeur par ID
                .orElseThrow(() -> new RuntimeException("Sender not found"));  // Lance exception si non trouvé
        User receiver = userRepository.findById(receiverId)  // Cherche le destinataire par ID
                .orElseThrow(() -> new RuntimeException("Receiver not found"));  // Lance exception si non trouvé

        Message message = new Message();  // Crée une nouvelle instance de Message
        message.setSender(sender);  // Définit l'expéditeur
        message.setReceiver(receiver);  // Définit le destinataire
        message.setContent(content);  // Définit le contenu du message
        message.setIsRead(false);  // Marque le message comme non lu (par défaut)

        return messageRepository.save(message);  // Sauvegarde le message en base et le retourne
    }

    //  MODIFIER UN MESSAGE (seul l'auteur peut modifier)
    @Transactional
    public Message updateMessage(Long messageId, Long userId, String newContent) {  // Modifie le contenu d'un message
        Message message = messageRepository.findById(messageId)  // Cherche le message par ID
                .orElseThrow(() -> new RuntimeException("Message not found"));  // Lance exception si non trouvé

        if (!message.getSender().getId().equals(userId)) {  // Vérifie que l'utilisateur est l'auteur
            throw new RuntimeException("You can only edit your own messages");  // Lance exception si non autorisé
        }

        message.setContent(newContent);  // Met à jour le contenu
        return messageRepository.save(message);  // Sauvegarde et retourne le message modifié
    }

    //  SUPPRIMER UN MESSAGE (seul l'auteur peut supprimer)
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {  // Supprime un message
        Message message = messageRepository.findById(messageId)  // Cherche le message par ID
                .orElseThrow(() -> new RuntimeException("Message not found"));  // Lance exception si non trouvé

        if (!message.getSender().getId().equals(userId)) {  // Vérifie que l'utilisateur est l'auteur
            throw new RuntimeException("You can only delete your own messages");  // Lance exception si non autorisé
        }

        messageRepository.delete(message);  // Supprime le message de la base
    }

    //  SUPPRIMER TOUTE LA CONVERSATION (supprime tous les messages entre deux utilisateurs)
    @Transactional
    public int deleteConversation(Long userId1, Long userId2) {  // Supprime toute la conversation
        List<Message> conversation = messageRepository.findConversation(userId1, userId2);  // Récupère tous les messages entre les deux
        int count = conversation.size();  // Compte le nombre de messages supprimés
        messageRepository.deleteAll(conversation);  // Supprime tous les messages de la conversation
        return count;  // Retourne le nombre de messages supprimés
    }

    //  MARQUER TOUS LES MESSAGES D'UNE CONVERSATION COMME LUS
    @Transactional
    public int markAllAsRead(Long currentUserId, Long otherUserId) {  // Marque tous les messages comme lus
        // Récupère tous les messages de la conversation où le destinataire est l'utilisateur courant ET non lus
        List<Message> unreadMessages = messageRepository.findConversation(otherUserId, currentUserId)
                .stream()  // Transforme en flux
                .filter(m -> m.getReceiver().getId().equals(currentUserId) && !m.getIsRead())  // Garde uniquement les messages non lus reçus
                .collect(Collectors.toList());  // Collecte dans une liste

        for (Message m : unreadMessages) {  // Parcourt chaque message non lu
            m.setIsRead(true);  // Marque comme lu
        }
        messageRepository.saveAll(unreadMessages);  // Sauvegarde tous les messages modifiés

        return unreadMessages.size();  // Retourne le nombre de messages marqués comme lus
    }

    // Méthode pour compter les messages non lus entre deux utilisateurs spécifiques
    public long getUnreadCount(Long userId, Long contactId) {
        return messageRepository.countUnreadBetweenUsers(contactId, userId);  // Appelle la requête JPQL personnalisée
    }

    // Méthode pour compter le nombre total de messages non lus d'un utilisateur
    public long getTotalUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);  // Appelle la requête JPQL personnalisée
    }

    // Méthode pour marquer tous les messages d'un expéditeur comme lus (par le destinataire)
    @Transactional
    public void markMessagesAsRead(Long userId, Long senderId) {
        messageRepository.markAsRead(senderId, userId);  // Appelle la requête de mise à jour en masse
    }

    // Méthode pour marquer un message spécifique comme lu
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        messageRepository.findById(messageId).ifPresent(message -> {  // Cherche le message; s'il existe
            if (message.getReceiver().getId().equals(userId) && !message.getIsRead()) {  // Vérifie que l'utilisateur est le destinataire ET message non lu
                message.setIsRead(true);  // Marque comme lu
                messageRepository.save(message);  // Sauvegarde le message
            }
        });
    }
}