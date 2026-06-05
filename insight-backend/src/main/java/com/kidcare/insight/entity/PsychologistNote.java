package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, Column, ManyToOne, JoinColumn)
import java.time.LocalDate;  // Importe la classe LocalDate pour la date de la session
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure de création de la note

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "psychologist_notes")  // Spécifie le nom de la table dans la base de données ("psychologist_notes")
public class PsychologistNote {  // Déclare l'entité représentant une note rédigée par un psychologue pour un enfant

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de la note du psychologue

    @Column(nullable = false, columnDefinition = "TEXT")  // Colonne obligatoire (ne peut pas être nulle), type TEXT pour les longs textes
    private String note;  // Contenu de la note rédigée par le psychologue (observations, diagnostic, recommandations)

    @Column(name = "session_date")  // Mappe ce champ à la colonne "session_date" de la table
    private LocalDate sessionDate;  // Date de la session de consultation (permet de lier la note à une séance spécifique)

    @ManyToOne  // Relation Many-to-One avec l'entité Child (plusieurs notes pour un même enfant)
    @JoinColumn(name = "child_id")  // Spécifie la colonne de jointure "child_id" qui référence la table des enfants
    private Child child;  // L'enfant concerné par cette note

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs notes pour un même psychologue)
    @JoinColumn(name = "psychologist_id")  // Spécifie la colonne de jointure "psychologist_id" qui référence la table des utilisateurs
    private User psychologist;  // Le psychologue qui a rédigé cette note

    @Column(name = "created_at")  // Mappe ce champ à la colonne "created_at"
    private LocalDateTime createdAt;  // Date et heure de création de la note (quand elle a été saisie dans le système)

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID

    public String getNote() { return note; }  // Getter pour le contenu de la note
    public void setNote(String note) { this.note = note; }  // Setter pour le contenu de la note

    public LocalDate getSessionDate() { return sessionDate; }  // Getter pour la date de session
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }  // Setter pour la date de session

    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé

    public User getPsychologist() { return psychologist; }  // Getter pour le psychologue auteur
    public void setPsychologist(User psychologist) { this.psychologist = psychologist; }  // Setter pour le psychologue auteur

    public LocalDateTime getCreatedAt() { return createdAt; }  // Getter pour la date de création
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  // Setter pour la date de création
}