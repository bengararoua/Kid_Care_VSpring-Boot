package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Notification;  // Importe l'entité Notification pour les opérations de base de données
import com.kidcare.insight.entity.User;  // Importe l'entité User pour filtrer les notifications par utilisateur
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.data.jpa.repository.Modifying;  // Importe l'annotation pour les requêtes de modification (UPDATE/DELETE)
import org.springframework.data.jpa.repository.Query;  // Importe l'annotation pour écrire des requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;  // Importe l'annotation pour lier les paramètres aux requêtes JPQL
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions (commit/rollback)

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface NotificationRepository extends JpaRepository<Notification, Long> {  // Déclare l'interface du repository pour l'entité Notification avec ID de type Long

    // ===== MÉTHODES DÉRIVÉES (générées automatiquement) =====

    // Méthode dérivée pour trouver toutes les notifications d'un utilisateur, triées par date de création décroissante
    List<Notification> findByUserOrderByCreatedAtDesc(User user);  // "findByUser" = filtre par utilisateur, "OrderByCreatedAtDesc" = tri du plus récent au plus ancien

    // Méthode dérivée pour trouver les notifications NON LUES d'un utilisateur, triées par date décroissante
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);  // "findByUserAndIsReadFalse" = filtre par utilisateur ET non lues

    // Méthode dérivée pour compter le nombre de notifications NON LUES d'un utilisateur
    long countByUserAndIsReadFalse(User user);  // Retourne le nombre de notifications où isRead = false

    // ===== REQUÊTE DE MISE À JOUR EN MASSE =====

    // Requête JPQL personnalisée pour marquer toutes les notifications d'un utilisateur comme lues
    @Modifying  // Indique que la requête modifie les données (UPDATE) et non juste une lecture
    @Transactional  // Exécute la requête dans une transaction (commit automatique)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")  // Requête JPQL : met à jour les notifications non lues
    void markAllAsRead(@Param("user") User user);  // Marque toutes les notifications non lues comme lues en une seule opération

    // ===== MÉTHODES DE SUPPRESSION MASSIVE =====

    // Méthode dérivée pour supprimer toutes les notifications d'un utilisateur
    @Modifying  // Nécessaire car c'est une suppression en masse
    @Transactional  // Transaction pour la suppression
    void deleteByUser(User user);  // Supprime toutes les notifications associées à un utilisateur (quand il supprime son compte)

    // Méthode dérivée pour compter le nombre TOTAL de notifications d'un utilisateur (lues + non lues)
    long countByUser(User user);  // Retourne le nombre total de notifications pour un utilisateur

}