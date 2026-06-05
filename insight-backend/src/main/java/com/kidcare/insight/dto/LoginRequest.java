package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class LoginRequest {  // Déclare la classe DTO pour la requête de connexion (utilisée par AuthController)

    private String email;  // Déclare l'email de l'utilisateur (servant d'identifiant unique pour la connexion)
    private String password;  // Déclare le mot de passe de l'utilisateur (sera vérifié par Spring Security en comparant avec le hash BCrypt)

    // Getter pour récupérer l'email
    public String getEmail() { return email; }
    
    // Setter pour définir l'email
    public void setEmail(String email) { this.email = email; }
    
    // Getter pour récupérer le mot de passe
    public String getPassword() { return password; }
    
    // Setter pour définir le mot de passe
    public void setPassword(String password) { this.password = password; }
}