package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class RegisterRequest {  // Déclare la classe DTO pour la requête d'inscription d'un nouvel utilisateur (utilisée par AuthController)

    private String name;  // Déclare le nom complet de l'utilisateur (affiché dans l'interface)
    private String email;  // Déclare l'email de l'utilisateur (servira d'identifiant unique pour la connexion)
    private String password;  // Déclare le mot de passe de l'utilisateur (sera hashé avant d'être stocké en base)
    private String role;  // Déclare le rôle de l'utilisateur ("parent", "teacher", "psychologist")

    // Getter pour récupérer le nom de l'utilisateur
    public String getName() { return name; }
    
    // Setter pour définir le nom de l'utilisateur
    public void setName(String name) { this.name = name; }
    
    // Getter pour récupérer l'email
    public String getEmail() { return email; }
    
    // Setter pour définir l'email
    public void setEmail(String email) { this.email = email; }
    
    // Getter pour récupérer le mot de passe
    public String getPassword() { return password; }
    
    // Setter pour définir le mot de passe
    public void setPassword(String password) { this.password = password; }
    
    // Getter pour récupérer le rôle
    public String getRole() { return role; }
    
    // Setter pour définir le rôle
    public void setRole(String role) { this.role = role; }
}