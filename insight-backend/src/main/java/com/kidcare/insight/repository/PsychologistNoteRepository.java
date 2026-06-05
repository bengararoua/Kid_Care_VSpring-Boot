package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.PsychologistNote;  // Importe l'entité PsychologistNote pour les opérations de base de données
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.data.jpa.repository.Query;  // Importe l'annotation pour écrire des requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;  // Importe l'annotation pour lier les paramètres aux requêtes JPQL
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository

import java.util.List;  // Importe l'interface List pour retourner une collection de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface PsychologistNoteRepository extends JpaRepository<PsychologistNote, Long> {  // Déclare l'interface du repository pour l'entité PsychologistNote avec ID de type Long
    
    // Requête JPQL personnalisée pour trouver toutes les notes d'un enfant (par son ID), triées par date de session décroissante
    @Query("SELECT n FROM PsychologistNote n WHERE n.child.id = :childId ORDER BY n.sessionDate DESC")
    List<PsychologistNote> findByChildIdOrderBySessionDateDesc(@Param("childId") Long childId);  // Retourne les notes du plus récent au plus ancien (par date de session)

}