package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class TokenCheckRequest {  // Déclare la classe DTO pour la vérification d'un token de réinitialisation (utilisée par PasswordResetController)

    private String email;  // Déclare l'email de l'utilisateur (permet de vérifier que le token correspond au bon compte)
    private String token;  // Déclare le token JWT temporaire à vérifier (reçu par email ou depuis le frontend)

    // Getter pour récupérer l'email
    public String getEmail() { return email; }
    
    // Setter pour définir l'email
    public void setEmail(String email) { this.email = email; }
    
    // Getter pour récupérer le token
    public String getToken() { return token; }
    
    // Setter pour définir le token
    public void setToken(String token) { this.token = token; }
}