package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.BehavioralTest;  // Importe l'entité BehavioralTest pour les opérations de base de données
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour filtrer les tests par enfant
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base

import java.util.List;  // Importe l'interface List pour retourner une collection de résultats

public interface BehavioralTestRepository extends JpaRepository<BehavioralTest, Long> {  // Déclare l'interface du repository pour l'entité BehavioralTest avec ID de type Long

    // Méthode pour trouver tous les tests comportementaux d'un enfant, triés par date de création décroissante
    List<BehavioralTest> findByChildOrderByCreatedAtDesc(Child child);  // "findByChild" = filtre par l'entité Child, "OrderByCreatedAtDesc" = tri par date de création du plus récent au plus ancien

}