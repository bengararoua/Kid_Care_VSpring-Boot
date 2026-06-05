package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Routine;  // Importe l'entité Routine pour les opérations de base de données
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour filtrer les routines par enfant
import com.kidcare.insight.entity.User;  // Importe l'entité User pour filtrer les routines par créateur
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats

public interface RoutineRepository extends JpaRepository<Routine, Long> {  // Déclare l'interface du repository pour l'entité Routine avec ID de type Long

    // Méthode dérivée pour trouver toutes les routines d'un enfant, triées par jour de la semaine (croissant) puis par heure (croissante)
    List<Routine> findByChildOrderByDayOfWeekAscTimeAsc(Child child);  // "findByChild" = filtre par enfant, "OrderByDayOfWeekAsc" = tri par jour (lundi→dimanche), "TimeAsc" = tri par heure (07:00 avant 08:00)

    // Méthode dérivée pour trouver les routines d'un enfant créées par un utilisateur spécifique
    List<Routine> findByChildAndUser(Child child, User user);  // "findByChildAndUser" = filtre par enfant ET par créateur

    // Méthode dérivée pour récupérer toutes les routines d'un enfant (sans tri spécifique)
    List<Routine> findByChild(Child child);  // Retourne les routines dans l'ordre d'insertion ou par ID par défaut

}