package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.ActionPlan;  // Importe l'entité ActionPlan pour les opérations de base de données
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour les méthodes de recherche par enfant
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base

import java.util.Optional;  // Importe la classe Optional pour gérer les résultats pouvant être nuls

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {  // Déclare l'interface du repository pour l'entité ActionPlan avec ID de type Long

    // Méthode pour trouver le plan d'action le plus récent d'un enfant (par son ID)
    Optional<ActionPlan> findTopByChildIdOrderByGeneratedDateDesc(Long childId);  // "findTop" = prend le premier, "ByChildId" = filtre par childId, "OrderByGeneratedDateDesc" = tri par date décroissante

    // Méthode pour trouver le premier plan d'action d'un enfant trié par date décroissante
    Optional<ActionPlan> findFirstByChildOrderByGeneratedDateDesc(Child child);  // "findFirst" = prend le premier, "ByChild" = filtre par l'entité Child, "OrderByGeneratedDateDesc" = tri par date décroissante

}