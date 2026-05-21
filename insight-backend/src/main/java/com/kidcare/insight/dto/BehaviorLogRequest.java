package com.kidcare.insight.dto;

import java.time.LocalDate;

public class BehaviorLogRequest {
    private Long childId;
    private Integer focusLevel;
    private String mood;
    private Double sleepHours;
    private Integer socialInteraction;
    private String note;
    private LocalDate logDate;

    // Getters et Setters
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
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