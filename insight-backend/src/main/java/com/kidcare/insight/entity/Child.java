package com.kidcare.insight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "children")
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Integer age;
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;
    
    @ManyToOne
    @JoinColumn(name = "psychologist_id")
    private User psychologist;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BehaviorLog> behaviors;
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recommendation> recommendations;
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PsychologistNote> psychologistNotes;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ========== GETTERS ET SETTERS ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public User getPsychologist() { return psychologist; }
    public void setPsychologist(User psychologist) { this.psychologist = psychologist; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public List<BehaviorLog> getBehaviors() { return behaviors; }
    public void setBehaviors(List<BehaviorLog> behaviors) { this.behaviors = behaviors; }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }

    public List<PsychologistNote> getPsychologistNotes() { return psychologistNotes; }
    public void setPsychologistNotes(List<PsychologistNote> psychologistNotes) { this.psychologistNotes = psychologistNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}