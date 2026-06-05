package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Recommendation;  // Importe l'entité Recommendation pour les opérations de base de données
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository

import java.util.List;  // Importe l'interface List pour retourner une collection de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {  // Déclare l'interface du repository pour l'entité Recommendation avec ID de type Long
    
    // Méthode dérivée pour trouver toutes les recommandations d'un enfant (par son ID), triées par date de création décroissante
    List<Recommendation> findByChildIdOrderByCreatedAtDesc(Long childId);  // "findByChildId" = filtre par ID de l'enfant, "OrderByCreatedAtDesc" = tri du plus récent au plus ancien

}