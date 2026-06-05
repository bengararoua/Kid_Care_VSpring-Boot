package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class PasswordEmailRequest {  // Déclare la classe DTO pour la requête de réinitialisation de mot de passe (utilisée par PasswordResetController)

    private String email;  // Déclare l'email de l'utilisateur qui a oublié son mot de passe

    // Getter pour récupérer l'email
    public String getEmail() { return email; }
    
    // Setter pour définir l'email
    public void setEmail(String email) { this.email = email; }
}