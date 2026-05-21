package com.kidcare.insight.dto;

import com.kidcare.insight.entity.Child;
import java.time.LocalDateTime;

public class ChildDTO {
    private Long id;
    private String name;
    private Integer age;
    private String notes;
    private Long parentId;
    private String parentName;
    private Long psychologistId;
    private String psychologistName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;

    public static ChildDTO fromChild(Child child) {
        ChildDTO dto = new ChildDTO();
        dto.setId(child.getId());
        dto.setName(child.getName());
        dto.setAge(child.getAge());
        dto.setNotes(child.getNotes());
        dto.setCreatedAt(child.getCreatedAt());

        if (child.getParent() != null) {
            dto.setParentId(child.getParent().getId());
            dto.setParentName(child.getParent().getName());
        }
        if (child.getPsychologist() != null) {
            dto.setPsychologistId(child.getPsychologist().getId());
            dto.setPsychologistName(child.getPsychologist().getName());
        }
        if (child.getTeacher() != null) {
            dto.setTeacherId(child.getTeacher().getId());
            dto.setTeacherName(child.getTeacher().getName());
        }

        return dto;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public Long getPsychologistId() { return psychologistId; }
    public void setPsychologistId(Long psychologistId) { this.psychologistId = psychologistId; }
    public String getPsychologistName() { return psychologistName; }
    public void setPsychologistName(String psychologistName) { this.psychologistName = psychologistName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}