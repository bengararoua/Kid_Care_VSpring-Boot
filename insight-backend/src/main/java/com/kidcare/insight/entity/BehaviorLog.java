package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import com.fasterxml.jackson.annotation.JsonIgnore;  // Importe l'annotation pour ignorer un champ lors de la sérialisation JSON (évite les boucles infinies)
import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn)
import java.time.LocalDate;  // Importe la classe LocalDate pour la date du log

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "behavior_logs")  // Spécifie le nom de la table dans la base de données ("behavior_logs")
public class BehaviorLog {  // Déclare l'entité représentant un journal de comportement quotidien pour un enfant

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique du journal de comportement
    
    @ManyToOne  // Relation Many-to-One avec l'entité Child (plusieurs logs pour un même enfant)
    @JoinColumn(name = "child_id")  // Spécifie la colonne de jointure "child_id" qui référence la table des enfants
    @JsonIgnore  // Ignore ce champ lors de la sérialisation JSON (empêche la boucle infinie Child ↔ BehaviorLog)
    private Child child;  // L'enfant auquel ce journal de comportement est associé
    
    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs logs pour un même utilisateur)
    @JoinColumn(name = "user_id")  // Spécifie la colonne de jointure "user_id" qui référence la table des utilisateurs
    @JsonIgnore  // Ignore ce champ lors de la sérialisation JSON (empêche la boucle infinie User ↔ BehaviorLog)
    private User user;  // L'utilisateur (parent, enseignant) qui a créé ce journal
    
    private Integer focusLevel;  // Niveau de concentration de l'enfant (échelle de 1 à 10 ou similaire)
    private String mood;  // Humeur de l'enfant (ex: "heureux", "triste", "énervé", "calme")
    private Double sleepHours;  // Nombre d'heures de sommeil de la nuit précédente (supporte les demi-heures)
    private Integer socialInteraction;  // Niveau d'interaction sociale (échelle de 1 à 10)
    private String note;  // Note libre pour des observations supplémentaires
    private LocalDate logDate;  // Date à laquelle le log a été enregistré

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID du log
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID du log
    
    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé
    
    public User getUser() { return user; }  // Getter pour l'utilisateur qui a créé le log
    public void setUser(User user) { this.user = user; }  // Setter pour l'utilisateur
    
    public Integer getFocusLevel() { return focusLevel; }  // Getter pour le niveau de concentration
    public void setFocusLevel(Integer focusLevel) { this.focusLevel = focusLevel; }  // Setter pour le niveau de concentration
    
    public String getMood() { return mood; }  // Getter pour l'humeur
    public void setMood(String mood) { this.mood = mood; }  // Setter pour l'humeur
    
    public Double getSleepHours() { return sleepHours; }  // Getter pour les heures de sommeil
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }  // Setter pour les heures de sommeil
    
    public Integer getSocialInteraction() { return socialInteraction; }  // Getter pour le niveau d'interaction sociale
    public void setSocialInteraction(Integer socialInteraction) { this.socialInteraction = socialInteraction; }  // Setter pour le niveau d'interaction sociale
    
    public String getNote() { return note; }  // Getter pour la note libre
    public void setNote(String note) { this.note = note; }  // Setter pour la note libre
    
    public LocalDate getLogDate() { return logDate; }  // Getter pour la date du log
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }  // Setter pour la date du log
}