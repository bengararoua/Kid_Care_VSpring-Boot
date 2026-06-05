package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class PasswordResetRequest {  // Déclare la classe DTO pour la requête de réinitialisation du mot de passe (utilisée par PasswordResetController)

    private String email;  // Déclare l'email de l'utilisateur (permet de vérifier que le token correspond au bon compte)
    private String token;  // Déclare le token JWT temporaire reçu par email (valide pour une durée limitée)
    private String password;  // Déclare le nouveau mot de passe (sera hashé avant stockage)
    private String passwordConfirmation;  // Déclare la confirmation du nouveau mot de passe (prévient les erreurs de saisie)

    // Getter pour récupérer l'email
    public String getEmail() { return email; }
    
    // Setter pour définir l'email
    public void setEmail(String email) { this.email = email; }
    
    // Getter pour récupérer le token de réinitialisation
    public String getToken() { return token; }
    
    // Setter pour définir le token de réinitialisation
    public void setToken(String token) { this.token = token; }
    
    // Getter pour récupérer le nouveau mot de passe
    public String getPassword() { return password; }
    
    // Setter pour définir le nouveau mot de passe
    public void setPassword(String password) { this.password = password; }
    
    // Getter pour récupérer la confirmation du nouveau mot de passe
    public String getPasswordConfirmation() { return passwordConfirmation; }
    
    // Setter pour définir la confirmation du nouveau mot de passe
    public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }
}