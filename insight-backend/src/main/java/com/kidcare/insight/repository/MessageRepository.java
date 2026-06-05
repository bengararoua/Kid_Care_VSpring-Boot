package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Message;  // Importe l'entité Message pour les opérations de base de données
import com.kidcare.insight.entity.User;  // Importe l'entité User pour les méthodes de recherche par expéditeur/destinataire
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.data.jpa.repository.Modifying;  // Importe l'annotation pour les requêtes de modification (UPDATE/DELETE)
import org.springframework.data.jpa.repository.Query;  // Importe l'annotation pour écrire des requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;  // Importe l'annotation pour lier les paramètres aux requêtes JPQL
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions (commit/rollback)

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface MessageRepository extends JpaRepository<Message, Long> {  // Déclare l'interface du repository pour l'entité Message avec ID de type Long

    // ===== MÉTHODES DÉRIVÉES (générées automatiquement) =====

    // Méthode dérivée pour trouver la conversation entre deux utilisateurs (dans les deux sens) triée par date croissante
    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtAsc(
            User sender1, User receiver1, User sender2, User receiver2);  // Cherche les messages où (sender=sender1 ET receiver=receiver1) OU (sender=sender2 ET receiver=receiver2)

    // Méthode dérivée pour trouver tous les messages non lus d'un destinataire
    List<Message> findByReceiverAndIsReadFalse(User receiver);  // Retourne tous les messages où le destinataire correspond ET isRead = false

    // Méthode dérivée pour compter les messages non lus d'un destinataire
    long countByReceiverAndIsReadFalse(User receiver);  // Retourne le nombre de messages non lus pour un utilisateur

    // ===== REQUÊTES JPQL PERSONNALISÉES =====

    // Requête JPQL pour trouver tous les messages reçus par un destinataire (par son ID)
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :receiverId")
    List<Message> findByReceiverId(@Param("receiverId") Long receiverId);  // Utilise l'ID plutôt que l'objet User complet

    // Requête JPQL pour trouver tous les messages envoyés par un expéditeur (par son ID)
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId")
    List<Message> findBySenderId(@Param("senderId") Long senderId);  // Utilise l'ID plutôt que l'objet User complet

    // Requête JPQL pour récupérer la conversation entre deux utilisateurs (la plus efficace)
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);  // Récupère les messages dans les deux sens, triés par date croissante

    // Requête JPQL pour compter les messages non lus entre deux utilisateurs spécifiques
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    long countUnreadBetweenUsers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);  // Utilisé pour les compteurs individuels

    // Requête JPQL pour compter tous les messages non lus d'un destinataire (par son ID)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.isRead = false")
    long countByReceiverIdAndIsReadFalse(@Param("receiverId") Long receiverId);  // Alternative à countByReceiverAndIsReadFalse avec ID

    // ===== REQUÊTE DE MISE À JOUR EN MASSE =====

    // Requête de modification pour marquer tous les messages d'une conversation comme lus
    @Modifying  // Indique que la requête modifie les données (UPDATE) et non juste une lecture
    @Transactional  // Exécute la requête dans une transaction (commit automatique)
    @Query("UPDATE Message m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);  // Met à jour tous les messages non lus d'un expéditeur vers un destinataire

}