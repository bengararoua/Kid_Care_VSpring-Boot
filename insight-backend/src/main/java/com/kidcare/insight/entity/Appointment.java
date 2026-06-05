package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure du rendez-vous

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "appointments")  // Spécifie le nom de la table dans la base de données ("appointments")
public class Appointment {  // Déclare l'entité représentant un rendez-vous entre deux utilisateurs (ex: parent avec psychologue)

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique du rendez-vous

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs rendez-vous pour un même expéditeur)
    @JoinColumn(name = "sender_id")  // Spécifie la colonne de jointure "sender_id" qui référence la table des utilisateurs
    private User sender;  // L'utilisateur qui a créé/planifié le rendez-vous (ex: parent, enseignant, psychologue)

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs rendez-vous pour un même destinataire)
    @JoinColumn(name = "receiver_id")  // Spécifie la colonne de jointure "receiver_id" qui référence la table des utilisateurs
    private User receiver;  // L'utilisateur qui est invité au rendez-vous (ex: psychologue, parent, enseignant)

    private String title;  // Titre du rendez-vous (ex: "Consultation de suivi", "Réunion pédagogique")

    private LocalDateTime scheduledAt;  // Date et heure prévues du rendez-vous

    private Integer duration = 30;  // Durée du rendez-vous en minutes (par défaut 30 minutes)

    private String location;  // Lieu du rendez-vous (ex: "Cabinet du Dr Martin", "En ligne (Zoom)", "École")

    private String notes;  // Notes ou remarques associées au rendez-vous (optionnel)

    private String type;  // Type de rendez-vous (ex: "consultation", "follow-up", "evaluation", "meeting")

    private String status = "pending";  // Statut du rendez-vous : "pending" (en attente), "confirmed" (confirmé), "cancelled" (annulé), "completed" (terminé)

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID

    public User getSender() { return sender; }  // Getter pour l'expéditeur (créateur du rendez-vous)
    public void setSender(User sender) { this.sender = sender; }  // Setter pour l'expéditeur

    public User getReceiver() { return receiver; }  // Getter pour le destinataire (participant invité)
    public void setReceiver(User receiver) { this.receiver = receiver; }  // Setter pour le destinataire

    public String getTitle() { return title; }  // Getter pour le titre du rendez-vous
    public void setTitle(String title) { this.title = title; }  // Setter pour le titre

    public LocalDateTime getScheduledAt() { return scheduledAt; }  // Getter pour la date et heure planifiée
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }  // Setter pour la date et heure

    public Integer getDuration() { return duration; }  // Getter pour la durée en minutes
    public void setDuration(Integer duration) { this.duration = duration; }  // Setter pour la durée

    public String getLocation() { return location; }  // Getter pour le lieu
    public void setLocation(String location) { this.location = location; }  // Setter pour le lieu

    public String getNotes() { return notes; }  // Getter pour les notes
    public void setNotes(String notes) { this.notes = notes; }  // Setter pour les notes

    public String getType() { return type; }  // Getter pour le type de rendez-vous
    public void setType(String type) { this.type = type; }  // Setter pour le type

    public String getStatus() { return status; }  // Getter pour le statut
    public void setStatus(String status) { this.status = status; }  // Setter pour le statut
}