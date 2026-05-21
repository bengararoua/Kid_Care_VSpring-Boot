package com.kidcare.insight.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "psychologist_notes")
public class PsychologistNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;
    
    @ManyToOne
    @JoinColumn(name = "psychologist_id")
    private User psychologist;
    
    private String note;
    private LocalDate sessionDate;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }
    public User getPsychologist() { return psychologist; }
    public void setPsychologist(User psychologist) { this.psychologist = psychologist; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
}