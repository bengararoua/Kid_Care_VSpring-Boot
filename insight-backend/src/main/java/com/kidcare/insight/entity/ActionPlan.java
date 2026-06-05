package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, Column, ManyToOne, JoinColumn)
import java.time.LocalDate;  // Importe la classe LocalDate pour la date de génération du plan

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "action_plans")  // Spécifie le nom de la table dans la base de données ("action_plans")
public class ActionPlan {  // Déclare l'entité représentant un plan d'action personnalisé généré à partir des résultats d'un test comportemental

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique du plan d'action

    @ManyToOne(fetch = FetchType.LAZY)  // Relation Many-to-One avec l'entité Child (plusieurs plans pour un enfant), chargement paresseux
    @JoinColumn(name = "child_id")  // Spécifie la colonne de jointure "child_id" qui référence la table des enfants
    private Child child;  // L'enfant pour lequel ce plan d'action a été généré

    @Column(name = "risk_level")  // Mappe ce champ à la colonne "risk_level" de la table
    private String riskLevel;  // Niveau de risque : "low" (faible), "moderate" (modéré) ou "high" (élevé)

    @Column(name = "generated_date")  // Mappe ce champ à la colonne "generated_date"
    private LocalDate generatedDate;  // Date à laquelle le plan a été généré

    @Column(name = "morning_activities", columnDefinition = "TEXT")  // Colonne "morning_activities" avec type TEXT (pour longs textes)
    private String morningActivities;  // Activités recommandées pour le matin (ex: "Méditation", "Lecture")

    @Column(name = "afternoon_activities", columnDefinition = "TEXT")  // Colonne "afternoon_activities" avec type TEXT
    private String afternoonActivities;  // Activités recommandées pour l'après-midi

    @Column(name = "evening_activities", columnDefinition = "TEXT")  // Colonne "evening_activities" avec type TEXT
    private String eveningActivities;  // Activités recommandées pour le soir

    @Column(name = "communication_tips", columnDefinition = "TEXT")  // Colonne "communication_tips" avec type TEXT
    private String communicationTips;  // Conseils de communication pour les parents/éducateurs

    @Column(name = "games_activities", columnDefinition = "TEXT")  // Colonne "games_activities" avec type TEXT
    private String gamesActivities;  // Jeux et activités ludiques recommandés

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID

    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé

    public String getRiskLevel() { return riskLevel; }  // Getter pour le niveau de risque
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }  // Setter pour le niveau de risque

    public LocalDate getGeneratedDate() { return generatedDate; }  // Getter pour la date de génération
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }  // Setter pour la date de génération

    public String getMorningActivities() { return morningActivities; }  // Getter pour les activités du matin
    public void setMorningActivities(String morningActivities) { this.morningActivities = morningActivities; }  // Setter pour les activités du matin

    public String getAfternoonActivities() { return afternoonActivities; }  // Getter pour les activités de l'après-midi
    public void setAfternoonActivities(String afternoonActivities) { this.afternoonActivities = afternoonActivities; }  // Setter pour les activités de l'après-midi

    public String getEveningActivities() { return eveningActivities; }  // Getter pour les activités du soir
    public void setEveningActivities(String eveningActivities) { this.eveningActivities = eveningActivities; }  // Setter pour les activités du soir

    public String getCommunicationTips() { return communicationTips; }  // Getter pour les conseils de communication
    public void setCommunicationTips(String communicationTips) { this.communicationTips = communicationTips; }  // Setter pour les conseils de communication

    public String getGamesActivities() { return gamesActivities; }  // Getter pour les jeux et activités
    public void setGamesActivities(String gamesActivities) { this.gamesActivities = gamesActivities; }  // Setter pour les jeux et activités
}