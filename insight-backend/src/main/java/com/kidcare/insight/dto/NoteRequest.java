package com.kidcare.insight.dto;

import java.time.LocalDate;

public class NoteRequest {
    private String note;
    private LocalDate sessionDate;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
}