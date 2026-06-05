package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, OneToMany, PrePersist)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure de création de l'enfant
import java.util.ArrayList;  // Importe la classe ArrayList pour initialiser les collections vides
import java.util.List;  // Importe l'interface List pour les collections d'objets enfants (logs, notes, etc.)

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "children")  // Spécifie le nom de la table dans la base de données ("children")
public class Child {  // Déclare l'entité représentant un enfant suivi dans l'application (par un parent, enseignant ou psychologue)

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de l'enfant
    
    private String name;  // Nom complet de l'enfant
    private Integer age;  // Âge de l'enfant en années
    private String notes;  // Notes ou observations concernant l'enfant
    
    @ManyToOne  // Relation Many-to-One vers User (plusieurs enfants peuvent avoir le même parent)
    @JoinColumn(name = "parent_id")  // Spécifie la colonne de jointure "parent_id" qui référence la table des utilisateurs
    private User parent;  // Parent associé à l'enfant (peut être null)
    
    @ManyToOne  // Relation Many-to-One vers User (plusieurs enfants peuvent avoir le même psychologue)
    @JoinColumn(name = "psychologist_id")  // Spécifie la colonne de jointure "psychologist_id"
    private User psychologist;  // Psychologue associé à l'enfant (pour le suivi professionnel)
    
    @ManyToOne  // Relation Many-to-One vers User (plusieurs enfants peuvent avoir le même enseignant)
    @JoinColumn(name = "teacher_id")  // Spécifie la colonne de jointure "teacher_id"
    private User teacher;  // Enseignant associé à l'enfant (pour le suivi scolaire)
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BehaviorLog> behaviors = new ArrayList<>();  // Liste des journaux de comportement de l'enfant (cascade: suppression enfant → suppression logs)
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Recommendation> recommendations = new ArrayList<>();  // Liste des recommandations pour l'enfant
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PsychologistNote> psychologistNotes = new ArrayList<>();  // Liste des notes du psychologue pour l'enfant
    
    // AJOUT DE LA RELATION AVEC BEHAVIORAL_TEST
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BehavioralTest> behavioralTests = new ArrayList<>();  // Liste des tests comportementaux passés par l'enfant
    
    private LocalDateTime createdAt;  // Date et heure de création de l'enfant dans le système
    
    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
    }
    
    public Child() {}  // Constructeur par défaut (obligatoire pour JPA)
    
    public Child(String name, Integer age) {  // Constructeur avec les champs obligatoires
        this.name = name;  // Assigne le nom reçu
        this.age = age;  // Assigne l'âge reçu
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public User getPsychologist() { return psychologist; }
    public void setPsychologist(User psychologist) { this.psychologist = psychologist; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public List<BehaviorLog> getBehaviors() { return behaviors; }
    public void setBehaviors(List<BehaviorLog> behaviors) { this.behaviors = behaviors; }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }

    public List<PsychologistNote> getPsychologistNotes() { return psychologistNotes; }
    public void setPsychologistNotes(List<PsychologistNote> psychologistNotes) { this.psychologistNotes = psychologistNotes; }
    
    public List<BehavioralTest> getBehavioralTests() { return behavioralTests; }
    public void setBehavioralTests(List<BehavioralTest> behavioralTests) { this.behavioralTests = behavioralTests; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Méthodes utilitaires pour gérer la relation bidirectionnelle avec BehavioralTest
    public void addBehavioralTest(BehavioralTest test) {  // Ajoute un test comportemental à l'enfant
        behavioralTests.add(test);  // Ajoute le test à la liste
        test.setChild(this);  // Définit cet enfant comme propriétaire du test (côté inverse)
    }
    
    public void removeBehavioralTest(BehavioralTest test) {  // Supprime un test comportemental de l'enfant
        behavioralTests.remove(test);  // Retire le test de la liste
        test.setChild(null);  // Supprime la référence vers cet enfant (côté inverse)
    }
    
    // Méthodes utilitaires pour gérer la relation bidirectionnelle avec BehaviorLog
    public void addBehaviorLog(BehaviorLog log) {  // Ajoute un journal de comportement à l'enfant
        behaviors.add(log);  // Ajoute le log à la liste
        log.setChild(this);  // Définit cet enfant comme propriétaire du log (côté inverse)
    }
    
    public void removeBehaviorLog(BehaviorLog log) {  // Supprime un journal de comportement de l'enfant
        behaviors.remove(log);  // Retire le log de la liste
        log.setChild(null);  // Supprime la référence vers cet enfant (côté inverse)
    }
}