package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class PasswordChangeRequest {  // Déclare la classe DTO pour la requête de changement de mot de passe (utilisée par ProfileController)

    private String current_password;  // Déclare l'ancien mot de passe (nécessaire pour vérifier l'identité avant de changer le mot de passe)
    private String password;  // Déclare le nouveau mot de passe (sera hashé avant d'être stocké en base)
    private String password_confirmation;  // Déclare la confirmation du nouveau mot de passe (pour éviter les erreurs de saisie)

    // Getter pour récupérer l'ancien mot de passe
    public String getCurrent_password() { return current_password; }
    
    // Setter pour définir l'ancien mot de passe
    public void setCurrent_password(String current_password) { this.current_password = current_password; }
    
    // Getter pour récupérer le nouveau mot de passe
    public String getPassword() { return password; }
    
    // Setter pour définir le nouveau mot de passe
    public void setPassword(String password) { this.password = password; }
    
    // Getter pour récupérer la confirmation du nouveau mot de passe
    public String getPassword_confirmation() { return password_confirmation; }
    
    // Setter pour définir la confirmation du nouveau mot de passe
    public void setPassword_confirmation(String password_confirmation) { this.password_confirmation = password_confirmation; }
}