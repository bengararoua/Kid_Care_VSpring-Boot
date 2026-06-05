package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import com.fasterxml.jackson.annotation.JsonFormat;  // Importe l'annotation pour définir le format de date lors de la sérialisation/désérialisation JSON
import java.time.LocalDate;  // Importe la classe LocalDate pour manipuler les dates (année, mois, jour) sans heure

public class NoteRequest {  // Déclare la classe DTO pour la requête de création d'une note psychologique (utilisée par PsychologistNoteController)

    private String note;  // Déclare le contenu texte de la note rédigée par le psychologue (observations, recommandations, compte-rendu)
    
    @JsonFormat(pattern = "yyyy-MM-dd")  // Spécifie le format de date attendu dans le JSON (ex: "2026-06-05") pour la désérialisation automatique
    private LocalDate sessionDate;  // Déclare la date de la session de consultation (permet de lier la note à une séance spécifique)

    // Constructeur par défaut (obligatoire pour la désérialisation JSON par Jackson)
    public NoteRequest() {}

    // Constructeur avec paramètres pour créer facilement une NoteRequest
    public NoteRequest(String note, LocalDate sessionDate) {
        this.note = note;  // Assigne le contenu de la note reçu en paramètre
        this.sessionDate = sessionDate;  // Assigne la date de session reçue en paramètre
    }

    // Getter pour récupérer le contenu de la note
    public String getNote() {
        return note;
    }

    // Setter pour définir le contenu de la note
    public void setNote(String note) {
        this.note = note;
    }

    // Getter pour récupérer la date de session
    public LocalDate getSessionDate() {
        return sessionDate;
    }

    // Setter pour définir la date de session
    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }
}