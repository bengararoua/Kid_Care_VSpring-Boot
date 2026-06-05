package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class RoutineDTO {  // Déclare la classe DTO pour transférer les données d'une routine entre le backend et le frontend

    private Long id;  // Déclare l'identifiant unique de la routine (auto-généré par la base de données)
    private Long childId;  // Déclare l'identifiant de l'enfant associé à cette routine
    private String dayOfWeek;  // Déclare le jour de la semaine (MONDAY, TUESDAY, etc.) pour la routine planifiée
    private String time;  // Déclare l'heure de la routine (format "HH:MM" comme "08:30" pour le matin)
    private String activity;  // Déclare le nom de l'activité (ex: "Brossage des dents", "Lecture du soir")
    private Integer duration;  // Déclare la durée estimée de l'activité en minutes
    private Boolean completed;  // Déclare si la routine a été complétée aujourd'hui (true/false)
    private Integer orderIndex;  // Déclare l'ordre d'affichage des routines (1, 2, 3... pour trier chronologiquement)
    private String createdBy;  // Déclare le nom de la personne qui a créé la routine (parent, enseignant, psychologue)
    private String createdByRole;  // Déclare le rôle de la personne qui a créé la routine ("parent", "teacher", "psychologist")

    // Getter pour récupérer l'ID de la routine
    public Long getId() { return id; }
    
    // Setter pour définir l'ID de la routine
    public void setId(Long id) { this.id = id; }
    
    // Getter pour récupérer l'ID de l'enfant
    public Long getChildId() { return childId; }
    
    // Setter pour définir l'ID de l'enfant
    public void setChildId(Long childId) { this.childId = childId; }
    
    // Getter pour récupérer le jour de la semaine
    public String getDayOfWeek() { return dayOfWeek; }
    
    // Setter pour définir le jour de la semaine
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    // Getter pour récupérer l'heure de la routine
    public String getTime() { return time; }
    
    // Setter pour définir l'heure de la routine
    public void setTime(String time) { this.time = time; }
    
    // Getter pour récupérer le nom de l'activité
    public String getActivity() { return activity; }
    
    // Setter pour définir le nom de l'activité
    public void setActivity(String activity) { this.activity = activity; }
    
    // Getter pour récupérer la durée de l'activité
    public Integer getDuration() { return duration; }
    
    // Setter pour définir la durée de l'activité
    public void setDuration(Integer duration) { this.duration = duration; }
    
    // Getter pour savoir si la routine est complétée
    public Boolean getCompleted() { return completed; }
    
    // Setter pour définir l'état de complétion de la routine
    public void setCompleted(Boolean completed) { this.completed = completed; }
    
    // Getter pour récupérer l'ordre d'affichage
    public Integer getOrderIndex() { return orderIndex; }
    
    // Setter pour définir l'ordre d'affichage
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    
    // Getter pour récupérer le nom du créateur
    public String getCreatedBy() { return createdBy; }
    
    // Setter pour définir le nom du créateur
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    // Getter pour récupérer le rôle du créateur
    public String getCreatedByRole() { return createdByRole; }
    
    // Setter pour définir le rôle du créateur
    public void setCreatedByRole(String createdByRole) { this.createdByRole = createdByRole; }
}