package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

public class RecommendationRequest {  // Déclare la classe DTO pour la requête de création d'une recommandation (utilisée par RecommendationController)

    private String title;  // Déclare le titre de la recommandation (ex: "Activités de communication quotidiennes")
    private String description;  // Déclare la description détaillée de la recommandation (explications, conseils pratiques)
    private String category;  // Déclare la catégorie de la recommandation ("communication", "social", "attention", "behavior")

    // Constructeur par défaut (obligatoire pour la désérialisation JSON par Jackson)
    public RecommendationRequest() {}

    // Constructeur avec paramètres pour créer facilement une RecommendationRequest (utile pour les tests)
    public RecommendationRequest(String title, String description, String category) {
        this.title = title;  // Assigne le titre reçu en paramètre
        this.description = description;  // Assigne la description reçue en paramètre
        this.category = category;  // Assigne la catégorie reçue en paramètre
    }

    // Getter pour récupérer le titre de la recommandation
    public String getTitle() {
        return title;
    }

    // Setter pour définir le titre de la recommandation
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter pour récupérer la description de la recommandation
    public String getDescription() {
        return description;
    }

    // Setter pour définir la description de la recommandation
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter pour récupérer la catégorie de la recommandation
    public String getCategory() {
        return category;
    }

    // Setter pour définir la catégorie de la recommandation
    public void setCategory(String category) {
        this.category = category;
    }
}