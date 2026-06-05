package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, Column, ManyToOne, JoinColumn, PrePersist)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure de création

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "recommendations")  // Spécifie le nom de la table dans la base de données ("recommendations")
public class Recommendation {  // Déclare l'entité représentant une recommandation personnalisée pour un enfant

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de la recommandation

    @Column(nullable = false)  // Colonne obligatoire (ne peut pas être nulle)
    private String title;  // Titre court de la recommandation (ex: "Activités de communication quotidiennes")

    @Column(nullable = false, columnDefinition = "TEXT")  // Colonne obligatoire, type TEXT pour les longues descriptions
    private String description;  // Description détaillée de la recommandation (explications, conseils pratiques)

    private String category;  // Catégorie de la recommandation : "communication", "social", "attention", "behavior"

    @Column(name = "is_completed")  // Mappe ce champ à la colonne "is_completed"
    private boolean completed = false;  // État d'avancement : false = à faire, true = complété

    @ManyToOne(fetch = FetchType.LAZY)  // Relation Many-to-One (plusieurs recommandations pour un enfant), chargement paresseux
    @JoinColumn(name = "child_id", nullable = false)  // Colonne de jointure "child_id" (ne peut pas être nulle)
    private Child child;  // L'enfant pour lequel cette recommandation a été créée

    @ManyToOne(fetch = FetchType.LAZY)  // Relation Many-to-One (plusieurs recommandations pour un auteur), chargement paresseux
    @JoinColumn(name = "author_id", nullable = false)  // Colonne de jointure "author_id" (ne peut pas être nulle)
    private User author;  // L'utilisateur (parent, enseignant, psychologue) qui a créé cette recommandation

    @Column(name = "created_at")  // Mappe ce champ à la colonne "created_at"
    private LocalDateTime createdAt;  // Date et heure de création de la recommandation

    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
    }

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour l'ID
    public void setId(Long id) { this.id = id; }  // Setter pour l'ID

    public String getTitle() { return title; }  // Getter pour le titre
    public void setTitle(String title) { this.title = title; }  // Setter pour le titre

    public String getDescription() { return description; }  // Getter pour la description
    public void setDescription(String description) { this.description = description; }  // Setter pour la description

    public String getCategory() { return category; }  // Getter pour la catégorie
    public void setCategory(String category) { this.category = category; }  // Setter pour la catégorie

    public boolean isCompleted() { return completed; }  // Getter pour l'état de complétion
    public void setCompleted(boolean completed) { this.completed = completed; }  // Setter pour l'état de complétion

    public Child getChild() { return child; }  // Getter pour l'enfant associé
    public void setChild(Child child) { this.child = child; }  // Setter pour l'enfant associé

    public User getAuthor() { return author; }  // Getter pour l'auteur de la recommandation
    public void setAuthor(User author) { this.author = author; }  // Setter pour l'auteur

    public LocalDateTime getCreatedAt() { return createdAt; }  // Getter pour la date de création
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  // Setter pour la date de création
}