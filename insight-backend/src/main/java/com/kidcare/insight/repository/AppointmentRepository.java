package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Appointment;  // Importe l'entité Appointment pour les opérations de base de données
import com.kidcare.insight.entity.User;  // Importe l'entité User pour les méthodes de recherche par utilisateur
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.data.jpa.repository.Query;  // Importe l'annotation pour écrire des requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;  // Importe l'annotation pour lier les paramètres aux requêtes JPQL
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour les comparaisons de dates
import java.util.List;  // Importe l'interface List pour les collections de résultats

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {  // Déclare l'interface du repository pour l'entité Appointment avec ID de type Long

    // Requête JPQL personnalisée pour trouver tous les rendez-vous d'un utilisateur (comme expéditeur ou destinataire)
    @Query("SELECT a FROM Appointment a WHERE a.sender = :user OR a.receiver = :user ORDER BY a.scheduledAt DESC")
    List<Appointment> findByUser(@Param("user") User user);  // Retourne tous les rendez-vous où l'utilisateur est sender ou receiver, triés par date décroissante

    // Trouve tous les rendez-vous créés par un expéditeur avec un statut spécifique
    List<Appointment> findBySenderAndStatus(User sender, String status);  // Exemple: findBySenderAndStatus(parent, "pending")

    // Trouve tous les rendez-vous reçus par un destinataire avec un statut spécifique
    List<Appointment> findByReceiverAndStatus(User receiver, String status);  // Exemple: findByReceiverAndStatus(parent, "confirmed")

    // Requête JPQL pour trouver les rendez-vous passés qui ne sont ni complétés ni annulés
    @Query("SELECT a FROM Appointment a WHERE a.scheduledAt < :now AND a.status NOT IN ('completed', 'cancelled')")
    List<Appointment> findPastAppointmentsNotCompleted(@Param("now") LocalDateTime now);  // Utilisé pour nettoyer ou mettre à jour les rendez-vous en retard

}