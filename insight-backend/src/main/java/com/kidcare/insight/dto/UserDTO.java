package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import com.kidcare.insight.entity.User;  // Importe l'entité User pour convertir ses données en DTO
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date de création

public class UserDTO {  // Déclare la classe DTO pour transférer les données d'un utilisateur au frontend (sans le mot de passe)

    private Long id;  // Déclare l'identifiant unique de l'utilisateur
    private String name;  // Déclare le nom complet de l'utilisateur
    private String email;  // Déclare l'email de l'utilisateur (utilisé comme identifiant)
    private String role;  // Déclare le rôle de l'utilisateur ("parent", "teacher", "psychologist")
    private Integer points;  // Déclare les points de gamification (récompenses, progression)
    private String avatar;  // Déclare l'URL ou le chemin de la photo de profil
    private LocalDateTime createdAt;  // Déclare la date et heure de création du compte

    // Constructeur par défaut (nécessaire pour la désérialisation JSON)
    public UserDTO() {}
    
    // Constructeur de conversion qui transforme une entité User en UserDTO
    public UserDTO(User user) {
        this.id = user.getId();  // Copie l'ID depuis l'entité
        this.name = user.getName();  // Copie le nom depuis l'entité
        this.email = user.getEmail();  // Copie l'email depuis l'entité
        this.role = user.getRole();  // Copie le rôle depuis l'entité
        this.points = user.getPoints();  // Copie les points depuis l'entité
        this.avatar = user.getAvatar();  // Copie l'avatar depuis l'entité
        this.createdAt = user.getCreatedAt();  // Copie la date de création depuis l'entité
    }
    
    // Getters
    public Long getId() { return id; }  // Getter pour récupérer l'ID
    public String getName() { return name; }  // Getter pour récupérer le nom
    public String getEmail() { return email; }  // Getter pour récupérer l'email
    public String getRole() { return role; }  // Getter pour récupérer le rôle
    public Integer getPoints() { return points; }  // Getter pour récupérer les points
    public String getAvatar() { return avatar; }  // Getter pour récupérer l'avatar
    public LocalDateTime getCreatedAt() { return createdAt; }  // Getter pour récupérer la date de création
    
    // Setters
    public void setId(Long id) { this.id = id; }  // Setter pour définir l'ID
    public void setName(String name) { this.name = name; }  // Setter pour définir le nom
    public void setEmail(String email) { this.email = email; }  // Setter pour définir l'email
    public void setRole(String role) { this.role = role; }  // Setter pour définir le rôle
    public void setPoints(Integer points) { this.points = points; }  // Setter pour définir les points
    public void setAvatar(String avatar) { this.avatar = avatar; }  // Setter pour définir l'avatar
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  // Setter pour définir la date de création
}