package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // Importe l'annotation pour ignorer certaines propriétés lors de la sérialisation JSON
import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn, PrePersist)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure d'envoi du message

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "messages")  // Spécifie le nom de la table dans la base de données ("messages")
public class Message {  // Déclare l'entité représentant un message échangé entre deux utilisateurs

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique du message

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs messages pour un même expéditeur)
    @JoinColumn(name = "sender_id")  // Spécifie la colonne de jointure "sender_id"
    @JsonIgnoreProperties({"messages", "password", "appointments", "children", "sentAppointments", "receivedAppointments"})
    private User sender;  // Utilisateur qui a envoyé le message

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs messages pour un même destinataire)
    @JoinColumn(name = "receiver_id")  // Spécifie la colonne de jointure "receiver_id"
    @JsonIgnoreProperties({"messages", "password", "appointments", "children", "sentAppointments", "receivedAppointments"})
    private User receiver;  // Utilisateur qui a reçu le message

    @Column(columnDefinition = "TEXT")  // Colonne de type TEXT pour les longs messages
    private String content;  // Contenu texte du message

    private Boolean isRead = false;  // Indique si le message a été lu par le destinataire (false = non lu, true = lu)

    private LocalDateTime createdAt;  // Date et heure d'envoi du message

    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
    }

    // Méthode utilitaire pour récupérer l'ID de l'expéditeur (sans exposer tout l'objet User)
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;  // Retourne l'ID de l'expéditeur ou null
    }

    // Méthode utilitaire pour récupérer l'ID du destinataire (sans exposer tout l'objet User)
    public Long getReceiverId() {
        return receiver != null ? receiver.getId() : null;  // Retourne l'ID du destinataire ou null
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}