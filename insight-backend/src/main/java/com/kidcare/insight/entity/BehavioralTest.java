package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn, PrePersist)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure de création du test

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "behavioral_tests")  // Spécifie le nom de la table dans la base de données ("behavioral_tests")
public class BehavioralTest {  // Déclare l'entité représentant un test comportemental passé par un enfant

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique du test comportemental

    @ManyToOne  // Relation Many-to-One avec l'entité Child (plusieurs tests pour un même enfant)
    @JoinColumn(name = "child_id")  // Spécifie la colonne de jointure "child_id" qui référence la table des enfants
    private Child child;  // L'enfant qui a passé le test

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs tests pour un même utilisateur)
    @JoinColumn(name = "user_id")  // Spécifie la colonne de jointure "user_id" qui référence la table des utilisateurs
    private User user;  // L'utilisateur (parent, enseignant, psychologue) qui a administré/rempli le test

    private String userRole;  // Rôle de l'utilisateur qui a passé le test ("parent", "teacher", "psychologist") pour l'historique

    @Column(columnDefinition = "JSON")  // Définit la colonne comme type JSON (stocke un objet JSON)
    private String responses;  // Map des réponses convertie en JSON (ex: {"comm_1":"always","comm_2":"usually",...})

    @Column(columnDefinition = "JSON")  // Définit la colonne comme type JSON
    private String scores;  // Scores calculés convertis en JSON (scores par catégorie et global)

    private String riskLevel;  // Niveau de risque : "low" (faible), "moderate" (modéré) ou "high" (élevé)

    @Column(columnDefinition = "TEXT")  // Colonne de type TEXT pour les longs messages de feedback
    private String feedbackMessage;  // Message de feedback personnalisé généré pour l'utilisateur

    @Column(columnDefinition = "JSON")  // Définit la colonne comme type JSON
    private String recommendations;  // Liste des recommandations convertie en JSON (ex: ["Recommandation 1", "Recommandation 2"])

    private LocalDateTime createdAt;  // Date et heure de création/soumission du test (auto-générée)

    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {  // Méthode appelée par JPA avant l'insertion
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
    }

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID du test
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID du test

    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé

    public User getUser() { return user; }  // Getter pour l'utilisateur qui a passé le test
    public void setUser(User user) { this.user = user; }  // Setter pour l'utilisateur

    public String getUserRole() { return userRole; }  // Getter pour le rôle de l'utilisateur
    public void setUserRole(String userRole) { this.userRole = userRole; }  // Setter pour le rôle de l'utilisateur

    public String getResponses() { return responses; }  // Getter pour les réponses en JSON
    public void setResponses(String responses) { this.responses = responses; }  // Setter pour les réponses JSON

    public String getScores() { return scores; }  // Getter pour les scores en JSON
    public void setScores(String scores) { this.scores = scores; }  // Setter pour les scores JSON

    public String getRiskLevel() { return riskLevel; }  // Getter pour le niveau de risque
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }  // Setter pour le niveau de risque

    public String getFeedbackMessage() { return feedbackMessage; }  // Getter pour le message de feedback
    public void setFeedbackMessage(String feedbackMessage) { this.feedbackMessage = feedbackMessage; }  // Setter pour le message de feedback

    public String getRecommendations() { return recommendations; }  // Getter pour les recommandations JSON
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }  // Setter pour les recommandations JSON

    public LocalDateTime getCreatedAt() { return createdAt; }  // Getter pour la date de création
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  // Setter pour la date de création
}