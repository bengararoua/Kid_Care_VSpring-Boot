package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // Importe l'annotation pour ignorer certaines propriétés lors de la sérialisation JSON
import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, OneToMany, PrePersist)
import java.time.LocalDate;  // Importe la classe LocalDate pour la dernière date de connexion
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date de création du compte
import java.util.ArrayList;  // Importe la classe ArrayList pour initialiser les collections vides
import java.util.List;  // Importe l'interface List pour les collections d'objets

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "users")  // Spécifie le nom de la table dans la base de données ("users")
public class User {  // Déclare l'entité représentant un utilisateur de l'application (parent, enseignant, psychologue)

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de l'utilisateur
    
    private String name;  // Nom complet de l'utilisateur (affiché dans l'interface)
    private String email;  // Email de l'utilisateur (servant d'identifiant unique pour la connexion)
    private String password;  // Mot de passe hashé (BCrypt) - jamais exposé en JSON
    private String role;  // Rôle de l'utilisateur : "parent", "teacher", "psychologist"
    
    private String avatar;  // URL ou chemin de la photo de profil (optionnel)
    private Integer points = 0;  // Points de gamification (récompenses, progression) - par défaut 0
    private LocalDate lastLogDate;  // Dernière date de connexion de l'utilisateur
    private LocalDateTime createdAt;  // Date et heure de création du compte
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)  // Un utilisateur peut envoyer plusieurs messages
    @JsonIgnoreProperties({"sender", "receiver"})  // Ignore les champs sender/receiver pour éviter les boucles infinies
    private List<Message> sentMessages = new ArrayList<>();  // Liste des messages envoyés par cet utilisateur
    
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)  // Un utilisateur peut recevoir plusieurs messages
    @JsonIgnoreProperties({"sender", "receiver"})  // Ignore les champs sender/receiver pour éviter les boucles infinies
    private List<Message> receivedMessages = new ArrayList<>();  // Liste des messages reçus par cet utilisateur
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)  // Un utilisateur peut créer plusieurs rendez-vous
    @JsonIgnoreProperties({"sender", "receiver"})  // Ignore les champs sender/receiver pour éviter les boucles infinies
    private List<Appointment> sentAppointments = new ArrayList<>();  // Liste des rendez-vous créés par cet utilisateur
    
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)  // Un utilisateur peut être invité à plusieurs rendez-vous
    @JsonIgnoreProperties({"sender", "receiver"})  // Ignore les champs sender/receiver pour éviter les boucles infinies
    private List<Appointment> receivedAppointments = new ArrayList<>();  // Liste des rendez-vous reçus par cet utilisateur

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)  // Un utilisateur peut avoir plusieurs notifications
    @JsonIgnoreProperties({"user"})  // Ignore le champ user pour éviter les boucles infinies
    private List<Notification> notifications = new ArrayList<>();  // Liste des notifications de l'utilisateur
    
    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
        if (points == null) {  // Vérifie si les points sont null
            points = 0;  // Initialise les points à 0
        }
    }
    
    // Méthodes utilitaires pour vérifier le rôle
    public boolean isParent() {  // Vérifie si l'utilisateur a le rôle PARENT
        return "parent".equalsIgnoreCase(role);  // Retourne true si le rôle est "parent" (insensible à la casse)
    }
    
    public boolean isTeacher() {  // Vérifie si l'utilisateur a le rôle ENSEIGNANT
        return "teacher".equalsIgnoreCase(role);  // Retourne true si le rôle est "teacher"
    }
    
    public boolean isPsychologist() {  // Vérifie si l'utilisateur a le rôle PSYCHOLOGUE
        return "psychologist".equalsIgnoreCase(role);  // Retourne true si le rôle est "psychologist"
    }

    // Méthode utilitaire pour ajouter des points de gamification
    public void incrementPoints(int pointsToAdd) {  // Ajoute des points à l'utilisateur
        if (this.points == null) {  // Vérifie si les points sont null
            this.points = 0;  // Initialise à 0
        }
        this.points += pointsToAdd;  // Ajoute le nombre de points
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getPoints() {  // Getter pour les points (avec sécurité null)
        if (points == null) {  // Vérifie si les points sont null
            return 0;  // Retourne 0 par défaut
        }
        return points; 
    }
    
    public void setPoints(Integer points) { 
        this.points = points; 
    }
    
    public LocalDate getLastLogDate() { return lastLogDate; }
    public void setLastLogDate(LocalDate lastLogDate) { this.lastLogDate = lastLogDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(List<Message> sentMessages) { this.sentMessages = sentMessages; }
    
    public List<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(List<Message> receivedMessages) { this.receivedMessages = receivedMessages; }
    
    public List<Appointment> getSentAppointments() { return sentAppointments; }
    public void setSentAppointments(List<Appointment> sentAppointments) { this.sentAppointments = sentAppointments; }
    
    public List<Appointment> getReceivedAppointments() { return receivedAppointments; }
    public void setReceivedAppointments(List<Appointment> receivedAppointments) { this.receivedAppointments = receivedAppointments; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
}