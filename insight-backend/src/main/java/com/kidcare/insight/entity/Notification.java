package com.kidcare.insight.entity;  // Déclare le package contenant les entités JPA pour la persistance en base de données

import com.fasterxml.jackson.core.JsonProcessingException;  // Importe l'exception pour les erreurs de traitement JSON
import com.fasterxml.jackson.core.type.TypeReference;  // Importe l'utilitaire pour les types génériques lors de la désérialisation JSON
import com.fasterxml.jackson.databind.ObjectMapper;  // Importe le mapper JSON pour convertir Map ↔ JSON
import jakarta.persistence.*;  // Importe toutes les annotations JPA (Entity, Table, Id, GeneratedValue, ManyToOne, JoinColumn, PrePersist)
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date et heure de création
import java.util.HashMap;  // Importe la classe HashMap pour créer des maps vides
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@Entity  // Déclare cette classe comme une entité JPA (sera mappée à une table en base de données)
@Table(name = "notifications")  // Spécifie le nom de la table dans la base de données ("notifications")
public class Notification {  // Déclare l'entité représentant une notification envoyée à un utilisateur

    private static final ObjectMapper objectMapper = new ObjectMapper();  // Crée un mapper JSON statique pour convertir Map ↔ JSON

    @Id  // Déclare ce champ comme clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // La base de données génère automatiquement l'ID (auto-incrément)
    private Long id;  // Identifiant unique de la notification

    @ManyToOne  // Relation Many-to-One avec l'entité User (plusieurs notifications pour un même utilisateur)
    @JoinColumn(name = "user_id", nullable = false)  // Colonne de jointure "user_id" (ne peut pas être nulle)
    private User user;  // Utilisateur destinataire de la notification

    @Column(nullable = false)  // Colonne obligatoire (ne peut pas être nulle)
    private String type;  // Type de notification (ex: "test_result", "new_message", "system", "reminder")

    @Column(nullable = false)  // Colonne obligatoire
    private String title;  // Titre de la notification (ex: "📋 Résultat du test comportemental")

    @Column(nullable = false, length = 500)  // Colonne obligatoire, limite de 500 caractères
    private String message;  // Message principal de la notification (description textuelle)

    @Column(columnDefinition = "TEXT")  // Colonne de type TEXT pour stocker des données JSON supplémentaires
    private String data;  // Données supplémentaires en JSON (ex: {"testId":42,"childId":5})

    @Column(nullable = false)  // Colonne obligatoire
    private Boolean isRead = false;  // Indique si la notification a été lue (false = non lue, true = lue)

    @Column(nullable = false)  // Colonne obligatoire
    private LocalDateTime createdAt;  // Date et heure de création de la notification

    @PrePersist  // Méthode exécutée automatiquement AVANT la première persistance (insertion en base)
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Définit automatiquement la date et heure actuelles
    }

    public Notification() {}  // Constructeur par défaut (obligatoire pour JPA)

    // Constructeur avec Map pour les données (recommandé)
    public Notification(User user, String type, String title, String message, Map<String, Object> data) {
        this.user = user;  // Assigne le destinataire
        this.type = type;  // Assigne le type de notification
        this.title = title;  // Assigne le titre
        this.message = message;  // Assigne le message
        setDataMap(data);  // Convertit la Map en JSON et la stocke dans le champ data
        this.isRead = false;  // Par défaut, la notification n'est pas lue
    }

    // Pour la compatibilité avec l'ancien constructeur (String data directement)
    public Notification(User user, String type, String title, String message, String data) {
        this.user = user;  // Assigne le destinataire
        this.type = type;  // Assigne le type
        this.title = title;  // Assigne le titre
        this.message = message;  // Assigne le message
        this.data = data;  // Stocke directement la chaîne JSON
        this.isRead = false;  // Par défaut, non lue
    }

    // Getter pour data en tant que Map (désérialisation JSON → Map)
    public Map<String, Object> getDataMap() {
        if (data == null || data.isEmpty()) {  // Vérifie si la chaîne JSON est vide ou nulle
            return new HashMap<>();  // Retourne une map vide
        }
        try {
            // Convertit la chaîne JSON en Map<String, Object>
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {  // En cas d'erreur de parsing
            return new HashMap<>();  // Retourne une map vide
        }
    }

    // Setter pour data à partir d'une Map (sérialisation Map → JSON)
    public void setDataMap(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {  // Vérifie si la map est vide ou nulle
            this.data = null;  // Stocke null
        } else {
            try {
                // Convertit la Map en chaîne JSON et la stocke
                this.data = objectMapper.writeValueAsString(dataMap);
            } catch (JsonProcessingException e) {  // En cas d'erreur de sérialisation
                this.data = null;  // Stocke null
            }
        }
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}