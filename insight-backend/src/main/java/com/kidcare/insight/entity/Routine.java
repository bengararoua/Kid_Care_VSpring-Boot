package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn)
import java.time.LocalTime;  // Importe la classe LocalTime pour l'heure de la routine (sans date)

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "routines")  // Spécifie le nom de la table dans la base de données ("routines")
public class Routine {  // Déclare l'entité représentant une routine quotidienne pour un enfant

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de la routine

    @ManyToOne  // Relation Many-to-One avec l'entité Child (plusieurs routines pour un même enfant)
    @JoinColumn(name = "child_id")  // Spécifie la colonne de jointure "child_id" qui référence la table des enfants
    private Child child;  // L'enfant pour lequel cette routine a été créée

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs routines pour un même utilisateur)
    @JoinColumn(name = "user_id")  // Spécifie la colonne de jointure "user_id" qui référence la table des utilisateurs
    private User user;  // L'utilisateur (parent, enseignant, psychologue) qui a créé cette routine

    private String dayOfWeek;  // Jour de la semaine : "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"

    private LocalTime time;  // Heure de la routine (format HH:MM, ex: "07:30" pour le matin)

    private String activity;  // Nom de l'activité (ex: "Se brosser les dents", "Lire une histoire")

    private Integer duration;  // Durée estimée de l'activité en minutes (ex: 15 pour 15 minutes)

    private Boolean completed = false;  // État d'avancement : false = à faire, true = complété (par défaut false)

    private Integer orderIndex = 0;  // Ordre d'affichage des routines (1, 2, 3... pour tri chronologique)

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID

    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé

    public User getUser() { return user; }  // Getter pour l'utilisateur créateur
    public void setUser(User user) { this.user = user; }  // Setter pour l'utilisateur créateur

    public String getDayOfWeek() { return dayOfWeek; }  // Getter pour le jour de la semaine
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }  // Setter pour le jour de la semaine

    public LocalTime getTime() { return time; }  // Getter pour l'heure de la routine
    public void setTime(LocalTime time) { this.time = time; }  // Setter pour l'heure de la routine

    public String getActivity() { return activity; }  // Getter pour le nom de l'activité
    public void setActivity(String activity) { this.activity = activity; }  // Setter pour le nom de l'activité

    public Integer getDuration() { return duration; }  // Getter pour la durée en minutes
    public void setDuration(Integer duration) { this.duration = duration; }  // Setter pour la durée

    public Boolean getCompleted() { return completed; }  // Getter pour l'état de complétion
    public void setCompleted(Boolean completed) { this.completed = completed; }  // Setter pour l'état de complétion

    public Integer getOrderIndex() { return orderIndex; }  // Getter pour l'ordre d'affichage
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }  // Setter pour l'ordre d'affichage
}