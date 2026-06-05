package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

public class TestSubmitRequest {  // Déclare la classe DTO pour la soumission des réponses du test comportemental (utilisée par BehavioralTestController)

    private Map<String, String> responses;  // Déclare une map contenant les réponses du test (clé = ID de la question, valeur = réponse choisie)

    // Getter pour récupérer la map des réponses
    public Map<String, String> getResponses() { return responses; }
    
    // Setter pour définir la map des réponses
    public void setResponses(Map<String, String> responses) { this.responses = responses; }
}