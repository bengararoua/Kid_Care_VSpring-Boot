package com.kidcare.insight.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "behavior_logs")
public class BehaviorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Integer focusLevel;
    private String mood;
    private Double sleepHours;
    private Integer socialInteraction;
    private String note;
    private LocalDate logDate;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getFocusLevel() { return focusLevel; }
    public void setFocusLevel(Integer focusLevel) { this.focusLevel = focusLevel; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public Double getSleepHours() { return sleepHours; }
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }
    public Integer getSocialInteraction() { return socialInteraction; }
    public void setSocialInteraction(Integer socialInteraction) { this.socialInteraction = socialInteraction; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
}