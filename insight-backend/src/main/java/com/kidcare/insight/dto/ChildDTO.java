package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour la convertir en DTO
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour manipuler les dates avec heure

public class ChildDTO {  // Déclare la classe DTO pour transférer les données d'un enfant au frontend (sans les relations circulaires)

    private Long id;  // Déclare l'identifiant unique de l'enfant
    private String name;  // Déclare le nom de l'enfant
    private Integer age;  // Déclare l'âge de l'enfant (en années)
    private String notes;  // Déclare les notes ou observations concernant l'enfant
    private Long parentId;  // Déclare l'ID du parent associé à l'enfant (peut être null)
    private String parentName;  // Déclare le nom du parent (pour affichage frontend)
    private Long psychologistId;  // Déclare l'ID du psychologue associé à l'enfant (peut être null)
    private String psychologistName;  // Déclare le nom du psychologue (pour affichage frontend)
    private Long teacherId;  // Déclare l'ID de l'enseignant associé à l'enfant (peut être null)
    private String teacherName;  // Déclare le nom de l'enseignant (pour affichage frontend)
    private LocalDateTime createdAt;  // Déclare la date et heure de création de l'enfant dans le système

    // IMPORTANT : On extrait les noms pour le Frontend
    public static ChildDTO fromChild(Child child) {  // Méthode statique de conversion (fabrique) qui transforme une entité Child en ChildDTO
        ChildDTO dto = new ChildDTO();  // Crée une nouvelle instance vide de ChildDTO
        dto.setId(child.getId());  // Copie l'ID de l'enfant depuis l'entité
        dto.setName(child.getName());  // Copie le nom de l'enfant depuis l'entité
        dto.setAge(child.getAge());  // Copie l'âge de l'enfant depuis l'entité
        dto.setNotes(child.getNotes());  // Copie les notes depuis l'entité
        dto.setCreatedAt(child.getCreatedAt());  // Copie la date de création depuis l'entité

        if (child.getParent() != null) {  // Vérifie si l'enfant a un parent associé (évite NullPointerException)
            dto.setParentId(child.getParent().getId());  // Copie l'ID du parent depuis l'entité
            dto.setParentName(child.getParent().getName());  // Copie le nom du parent depuis l'entité (évite un appel supplémentaire au backend)
        }
        
        if (child.getPsychologist() != null) {  // Vérifie si l'enfant a un psychologue associé
            dto.setPsychologistId(child.getPsychologist().getId());  // Copie l'ID du psychologue depuis l'entité
            dto.setPsychologistName(child.getPsychologist().getName());  // Copie le nom du psychologue depuis l'entité
        }
        
        if (child.getTeacher() != null) {  // Vérifie si l'enfant a un enseignant associé
            dto.setTeacherId(child.getTeacher().getId());  // Copie l'ID de l'enseignant depuis l'entité
            dto.setTeacherName(child.getTeacher().getName());  // Copie le nom de l'enseignant depuis l'entité
        }

        return dto;  // Retourne le DTO complètement rempli
    }

    // Getters et Setters
    public Long getId() { return id; }  // Getter pour récupérer l'ID de l'enfant
    public void setId(Long id) { this.id = id; }  // Setter pour définir l'ID de l'enfant

    public String getName() { return name; }  // Getter pour récupérer le nom de l'enfant
    public void setName(String name) { this.name = name; }  // Setter pour définir le nom de l'enfant

    public Integer getAge() { return age; }  // Getter pour récupérer l'âge de l'enfant
    public void setAge(Integer age) { this.age = age; }  // Setter pour définir l'âge de l'enfant

    public String getNotes() { return notes; }  // Getter pour récupérer les notes
    public void setNotes(String notes) { this.notes = notes; }  // Setter pour définir les notes

    public Long getParentId() { return parentId; }  // Getter pour récupérer l'ID du parent
    public void setParentId(Long parentId) { this.parentId = parentId; }  // Setter pour définir l'ID du parent

    public String getParentName() { return parentName; }  // Getter pour récupérer le nom du parent
    public void setParentName(String parentName) { this.parentName = parentName; }  // Setter pour définir le nom du parent

    public Long getPsychologistId() { return psychologistId; }  // Getter pour récupérer l'ID du psychologue
    public void setPsychologistId(Long psychologistId) { this.psychologistId = psychologistId; }  // Setter pour définir l'ID du psychologue

    public String getPsychologistName() { return psychologistName; }  // Getter pour récupérer le nom du psychologue
    public void setPsychologistName(String psychologistName) { this.psychologistName = psychologistName; }  // Setter pour définir le nom du psychologue

    public Long getTeacherId() { return teacherId; }  // Getter pour récupérer l'ID de l'enseignant
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }  // Setter pour définir l'ID de l'enseignant

    public String getTeacherName() { return teacherName; }  // Getter pour récupérer le nom de l'enseignant
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }  // Setter pour définir le nom de l'enseignant

    public LocalDateTime getCreatedAt() { return createdAt; }  // Getter pour récupérer la date de création
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  // Setter pour définir la date de création
}