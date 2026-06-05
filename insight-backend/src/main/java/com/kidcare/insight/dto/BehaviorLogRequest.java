package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import java.time.LocalDate;  // Importe la classe LocalDate pour manipuler les dates (année, mois, jour) sans heure

public class BehaviorLogRequest {  // Déclare la classe DTO pour la requête de création d'un journal de comportement (utilisée par BehaviorController)

    private Long childId;  // Déclare l'identifiant de l'enfant pour lequel le log est enregistré
    private Integer focusLevel;  // Déclare le niveau de concentration de l'enfant (échelle 1-10 ou similaire)
    private String mood;  // Déclare l'humeur de l'enfant (ex: "heureux", "triste", "énervé")
    private Double sleepHours;  // Déclare le nombre d'heures de sommeil de la nuit précédente
    private Integer socialInteraction;  // Déclare le niveau d'interaction sociale (échelle 1-10)
    private String note;  // Déclare une note libre pour des observations supplémentaires
    private LocalDate logDate;  // Déclare la date à laquelle le log a été enregistré
    
    // Getter pour récupérer l'ID de l'enfant
    public Long getChildId() { return childId; }
    
    // Setter pour définir l'ID de l'enfant
    public void setChildId(Long childId) { this.childId = childId; }
    
    // Getter pour récupérer le niveau de concentration
    public Integer getFocusLevel() { return focusLevel; }
    
    // Setter pour définir le niveau de concentration
    public void setFocusLevel(Integer focusLevel) { this.focusLevel = focusLevel; }
    
    // Getter pour récupérer l'humeur
    public String getMood() { return mood; }
    
    // Setter pour définir l'humeur
    public void setMood(String mood) { this.mood = mood; }
    
    // Getter pour récupérer les heures de sommeil
    public Double getSleepHours() { return sleepHours; }
    
    // Setter pour définir les heures de sommeil
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }
    
    // Getter pour récupérer le niveau d'interaction sociale
    public Integer getSocialInteraction() { return socialInteraction; }
    
    // Setter pour définir le niveau d'interaction sociale
    public void setSocialInteraction(Integer socialInteraction) { this.socialInteraction = socialInteraction; }
    
    // Getter pour récupérer la note
    public String getNote() { return note; }
    
    // Setter pour définir la note
    public void setNote(String note) { this.note = note; }
    
    // Getter pour récupérer la date du log
    public LocalDate getLogDate() { return logDate; }
    
    // Setter pour définir la date du log
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
}