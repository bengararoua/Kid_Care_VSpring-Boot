package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class MessageRequest {  // Déclare la classe DTO pour la requête d'envoi de message (utilisée par MessageController)

    private String content;  // Déclare le contenu texte du message (ce que l'utilisateur a écrit)

    // Getter pour récupérer le contenu du message
    public String getContent() { return content; }
    
    // Setter pour définir le contenu du message
    public void setContent(String content) { this.content = content; }
}