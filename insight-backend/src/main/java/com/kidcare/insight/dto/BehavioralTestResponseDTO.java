package com.kidcare.insight.dto;  // Déclare le package contenant les objets de transfert de données (DTO)

import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

public class BehavioralTestResponseDTO {  // Déclare la classe DTO pour la réponse du test comportemental (utilisée par BehavioralTestController)

    private Map<String, Object> questions;  // Déclare une map contenant les questions du test (clé = catégorie, valeur = liste des questions)

    private Map<String, CategoryInfo> categories;  // Déclare une map contenant les métadonnées des catégories (clé = nom catégorie, valeur = infos avec nom, icône, couleur)

    public static class CategoryInfo {  // Déclare une classe interne statique pour stocker les informations d'affichage d'une catégorie
        private String name;  // Déclare le nom affichable de la catégorie (ex: "Communication", "Social")
        private String icon;  // Déclare l'emoji ou icône associé à la catégorie (ex: "💬", "👥")
        private String color;  // Déclare la couleur hexadécimale associée à la catégorie (ex: "#3b82f6" pour bleu)

        public CategoryInfo(String name, String icon, String color) {  // Constructeur prenant le nom, l'icône et la couleur
            this.name = name;  // Assigne le nom reçu à la variable d'instance
            this.icon = icon;  // Assigne l'icône reçue à la variable d'instance
            this.color = color;  // Assigne la couleur reçue à la variable d'instance
        }
        
        // Getters et Setters
        public String getName() { return name; }  // Getter pour récupérer le nom de la catégorie
        public void setName(String name) { this.name = name; }  // Setter pour modifier le nom de la catégorie
        
        public String getIcon() { return icon; }  // Getter pour récupérer l'icône de la catégorie
        public void setIcon(String icon) { this.icon = icon; }  // Setter pour modifier l'icône de la catégorie
        
        public String getColor() { return color; }  // Getter pour récupérer la couleur de la catégorie
        public void setColor(String color) { this.color = color; }  // Setter pour modifier la couleur de la catégorie
    }

    // Getters et Setters pour BehavioralTestResponseDTO
    public Map<String, Object> getQuestions() { return questions; }  // Getter pour récupérer la map des questions
    public void setQuestions(Map<String, Object> questions) { this.questions = questions; }  // Setter pour définir la map des questions
    
    public Map<String, CategoryInfo> getCategories() { return categories; }  // Getter pour récupérer la map des catégories
    public void setCategories(Map<String, CategoryInfo> categories) { this.categories = categories; }  // Setter pour définir la map des catégories
}