package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class ChildRequest {  // Déclare la classe DTO pour les requêtes de création/mise à jour d'un enfant (utilisée par ChildController)

    private String name;  // Déclare le nom de l'enfant (champ obligatoire pour la création)
    private Integer age;  // Déclare l'âge de l'enfant en années (champ optionnel)
    private String notes;  // Déclare les notes ou observations concernant l'enfant (champ optionnel, texte libre)
    private Long parentId;  // Déclare l'identifiant du parent à associer à l'enfant (optionnel, pour l'affectation familiale)
    private Long psychologistId;  // Déclare l'identifiant du psychologue à associer à l'enfant (optionnel, pour le suivi professionnel)
    private Long teacherId;  // Déclare l'identifiant de l'enseignant à associer à l'enfant (optionnel, pour le suivi scolaire)
    
    // Getter pour récupérer le nom de l'enfant
    public String getName() { return name; }
    
    // Setter pour définir le nom de l'enfant
    public void setName(String name) { this.name = name; }
    
    // Getter pour récupérer l'âge de l'enfant
    public Integer getAge() { return age; }
    
    // Setter pour définir l'âge de l'enfant
    public void setAge(Integer age) { this.age = age; }
    
    // Getter pour récupérer les notes
    public String getNotes() { return notes; }
    
    // Setter pour définir les notes
    public void setNotes(String notes) { this.notes = notes; }
    
    // Getter pour récupérer l'ID du parent associé
    public Long getParentId() { return parentId; }
    
    // Setter pour définir l'ID du parent associé
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    // Getter pour récupérer l'ID du psychologue associé
    public Long getPsychologistId() { return psychologistId; }
    
    // Setter pour définir l'ID du psychologue associé
    public void setPsychologistId(Long psychologistId) { this.psychologistId = psychologistId; }
    
    // Getter pour récupérer l'ID de l'enseignant associé
    public Long getTeacherId() { return teacherId; }
    
    // Setter pour définir l'ID de l'enseignant associé
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}