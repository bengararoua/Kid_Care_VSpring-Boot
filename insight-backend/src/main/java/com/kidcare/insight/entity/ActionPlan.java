package com.kidcare.insight.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "action_plans")
public class ActionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;

    private String riskLevel;
    private LocalDate generatedDate;

    @Column(columnDefinition = "TEXT")
    private String morningActivities;

    @Column(columnDefinition = "TEXT")
    private String afternoonActivities;

    @Column(columnDefinition = "TEXT")
    private String eveningActivities;

    @Column(columnDefinition = "TEXT")
    private String communicationTips;

    @Column(columnDefinition = "TEXT")
    private String gamesActivities;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
    public String getMorningActivities() { return morningActivities; }
    public void setMorningActivities(String morningActivities) { this.morningActivities = morningActivities; }
    public String getAfternoonActivities() { return afternoonActivities; }
    public void setAfternoonActivities(String afternoonActivities) { this.afternoonActivities = afternoonActivities; }
    public String getEveningActivities() { return eveningActivities; }
    public void setEveningActivities(String eveningActivities) { this.eveningActivities = eveningActivities; }
    public String getCommunicationTips() { return communicationTips; }
    public void setCommunicationTips(String communicationTips) { this.communicationTips = communicationTips; }
    public String getGamesActivities() { return gamesActivities; }
    public void setGamesActivities(String gamesActivities) { this.gamesActivities = gamesActivities; }
}