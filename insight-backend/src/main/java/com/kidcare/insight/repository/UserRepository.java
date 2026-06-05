package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.User;  // Importe l'entité User pour les opérations de base de données
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats
import java.util.Optional;  // Importe la classe Optional pour gérer les résultats pouvant être nuls

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface UserRepository extends JpaRepository<User, Long> {  // Déclare l'interface du repository pour l'entité User avec ID de type Long

    // Méthode dérivée pour trouver un utilisateur par son email (retourne Optional pour gérer le cas où l'email n'existe pas)
    Optional<User> findByEmail(String email);  // "findByEmail" = recherche par champ email, retourne Optional (peut être vide)

    // Méthode dérivée pour vérifier si un email existe déjà en base (retourne true/false)
    boolean existsByEmail(String email);  // "existsByEmail" = vérifie l'existence d'un email, plus performant que findByEmail().isPresent()

    // Méthode dérivée pour trouver tous les utilisateurs ayant un rôle spécifique
    List<User> findByRole(String role);  // "findByRole" = filtre par le champ role (ex: "parent", "teacher", "psychologist")

}