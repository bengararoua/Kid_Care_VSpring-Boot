package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité BehaviorLog (journal de comportement) pour les opérations de base de données
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour filtrer les logs par enfant
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface BehaviorLogRepository extends JpaRepository<BehaviorLog, Long> {  // Déclare l'interface du repository pour l'entité BehaviorLog avec ID de type Long
    
    // Méthode pour trouver tous les logs d'un enfant par son ID, triés par date de log décroissante
    List<BehaviorLog> findByChildIdOrderByLogDateDesc(Long childId);  // "findByChildId" = filtre par l'ID de l'enfant, "OrderByLogDateDesc" = tri par date du plus récent au plus ancien
    
    // Méthode pour trouver tous les logs d'un enfant par son ID (sans tri spécifique)
    List<BehaviorLog> findByChildId(Long childId);  // Retourne les logs dans l'ordre d'insertion (ou par ID par défaut)
    
    // Méthode pour trouver tous les logs d'un enfant en passant l'objet Child complet
    List<BehaviorLog> findByChild(Child child);  // Alternative à findByChildId, utilise l'objet Child au lieu de son ID
    
    // Méthode pour trouver tous les logs d'un enfant (objet Child) triés par date décroissante
    List<BehaviorLog> findByChildOrderByLogDateDesc(Child child);  // Combine la recherche par objet Child avec un tri par date
    
    // Méthode pour trouver les 7 derniers logs d'un enfant (une semaine), triés par date décroissante
    List<BehaviorLog> findTop7ByChildOrderByLogDateDesc(Child child);  // "findTop7" = limite à 7 résultats, "ByChild" = filtre par enfant, "OrderByLogDateDesc" = tri par date décroissante

}