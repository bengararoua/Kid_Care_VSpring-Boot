package com.kidcare.insight.dto;

public class ChildRequest {
    private String name;
    private Integer age;
    private Long parentId;
    private Long psychologistId;
    private Long teacherId;
    private String notes;

    // ========== GETTERS ET SETTERS ==========
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getPsychologistId() { return psychologistId; }
    public void setPsychologistId(Long psychologistId) { this.psychologistId = psychologistId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}