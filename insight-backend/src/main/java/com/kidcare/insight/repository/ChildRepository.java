package com.kidcare.insight.repository;  // Déclare le package contenant les repositories JPA pour l'accès aux données

import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour les opérations de base de données
import com.kidcare.insight.entity.User;  // Importe l'entité User pour les méthodes de recherche par utilisateur (parent, teacher, psychologist)
import org.springframework.data.jpa.repository.JpaRepository;  // Importe l'interface JpaRepository qui fournit les méthodes CRUD de base
import org.springframework.data.jpa.repository.Query;  // Importe l'annotation pour écrire des requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;  // Importe l'annotation pour lier les paramètres aux requêtes JPQL
import org.springframework.stereotype.Repository;  // Importe l'annotation pour déclarer un composant Spring de type Repository

import java.util.List;  // Importe l'interface List pour retourner des collections de résultats

@Repository  // Déclare cette interface comme un composant Spring de type Repository (injectable)
public interface ChildRepository extends JpaRepository<Child, Long> {  // Déclare l'interface du repository pour l'entité Child avec ID de type Long
    
    // ===== REQUÊTES JPQL PERSONNALISÉES =====
    
    // Récupère tous les enfants associés à un utilisateur (parent, teacher ou psychologist)
    @Query("SELECT c FROM Child c WHERE " +  // Requête JPQL : sélectionne les enfants c
           "c.parent.id = :userId OR " +     // Condition : l'utilisateur est le parent
           "c.teacher.id = :userId OR " +    // OU l'utilisateur est l'enseignant
           "c.psychologist.id = :userId")    // OU l'utilisateur est le psychologue
    List<Child> findChildrenByUserId(@Param("userId") Long userId);  // Méthode pour trouver tous les enfants accessibles par un utilisateur (quel que soit son rôle)
    
    // ===== MÉTHODES PAR ID (utilisent l'ID de l'utilisateur) =====
    
    // Recherche par ID du parent (Long) - méthode dérivée
    List<Child> findByParentId(Long parentId);  // Retourne tous les enfants dont le parent a l'ID spécifié
    
    // Recherche par ID de l'enseignant (Long) - méthode dérivée
    List<Child> findByTeacherId(Long teacherId);  // Retourne tous les enfants dont l'enseignant a l'ID spécifié
    
    // Recherche par ID du psychologue (Long) - méthode dérivée
    List<Child> findByPsychologistId(Long psychologistId);  // Retourne tous les enfants dont le psychologue a l'ID spécifié
    
    // ===== MÉTHODES PAR OBJET USER (plus orientées objet) =====
    
    // Recherche par objet Parent (User) - méthode dérivée
    List<Child> findByParent(User parent);  // Retourne tous les enfants dont le parent est l'objet User passé
    
    // Recherche par objet Teacher (User) - méthode dérivée
    List<Child> findByTeacher(User teacher);  // Retourne tous les enfants dont l'enseignant est l'objet User passé
    
    // Recherche par objet Psychologist (User) - méthode dérivée
    List<Child> findByPsychologist(User psychologist);  // Retourne tous les enfants dont le psychologue est l'objet User passé
    
    // ===== MÉTHODES DE SÉCURITÉ (vérification d'accès) =====
    
    // Recherche un enfant par son ID et vérifie que l'utilisateur y a accès (pour sécurité)
    @Query("SELECT c FROM Child c WHERE c.id = :childId AND " +  // Requête JPQL : sélectionne l'enfant par ID
           "(:userId = c.parent.id OR :userId = c.teacher.id OR :userId = c.psychologist.id)")  // Vérifie que l'utilisateur a accès (parent/teacher/psychologist)
    Child findByIdAndUserAccess(@Param("childId") Long childId, @Param("userId") Long userId);  // Retourne l'enfant uniquement si l'utilisateur y a accès
    
    // Vérifier si un enfant existe et l'utilisateur a accès (retourne true/false)
    @Query("SELECT COUNT(c) > 0 FROM Child c WHERE c.id = :childId AND " +  // Requête JPQL : compte et vérifie l'existence
           "(:userId = c.parent.id OR :userId = c.teacher.id OR :userId = c.psychologist.id)")  // Vérifie l'accès
    boolean existsByIdAndUserAccess(@Param("childId") Long childId, @Param("userId") Long userId);  // Retourne true si l'enfant existe ET l'utilisateur y a accès
    
    // ===== MÉTHODES DE COMPTAGE =====
    
    // Compter le nombre d'enfants associés à un parent
    long countByParentId(Long parentId);  // Retourne le nombre d'enfants dont le parent a l'ID spécifié
    
    // Compter le nombre d'enfants associés à un enseignant
    long countByTeacherId(Long teacherId);  // Retourne le nombre d'enfants dont l'enseignant a l'ID spécifié
    
    // Compter le nombre d'enfants associés à un psychologue
    long countByPsychologistId(Long psychologistId);  // Retourne le nombre d'enfants dont le psychologue a l'ID spécifié
    
    // ===== MÉTHODES D'EXISTENCE (vérification simple) =====
    
    // Vérifier si un enfant existe et est associé à un parent
    boolean existsByIdAndParentId(Long id, Long parentId);  // Retourne true si l'enfant existe ET a ce parent
    
    // Vérifier si un enfant existe et est associé à un enseignant
    boolean existsByIdAndTeacherId(Long id, Long teacherId);  // Retourne true si l'enfant existe ET a cet enseignant
    
    // Vérifier si un enfant existe et est associé à un psychologue
    boolean existsByIdAndPsychologistId(Long id, Long psychologistId);  // Retourne true si l'enfant existe ET a ce psychologue
    
    // ===== MÉTHODES DE RECHERCHE PAR ABSENCE =====
    
    // Recherche les enfants sans psychologue assigné
    List<Child> findByPsychologistIsNull();  // Retourne les enfants qui n'ont pas encore de psychologue
    
    // Recherche les enfants sans enseignant assigné
    List<Child> findByTeacherIsNull();  // Retourne les enfants qui n'ont pas encore d'enseignant assigné
    
    // ===== MÉTHODES DE RECHERCHE TEXTUELLE =====
    
    // Recherche les enfants par nom (recherche partielle, insensible à la casse)
    List<Child> findByNameContainingIgnoreCase(String name);  // Ex: "Lucas" trouvera "Lucas Martin", "LUCAS", etc.
    
    // ===== MÉTHODES DE RECHERCHE PAR ÂGE =====
    
    // Recherche les enfants par âge exact
    List<Child> findByAge(Integer age);  // Retourne tous les enfants d'un âge spécifique (ex: 7 ans)
    
    // Recherche les enfants par âge entre deux valeurs
    List<Child> findByAgeBetween(Integer minAge, Integer maxAge);  // Retourne les enfants entre 5 et 10 ans par exemple
    
    // ===== MÉTHODES DE SUPPRESSION MASSIVE =====
    
    // Supprimer tous les enfants d'un parent
    void deleteByParentId(Long parentId);  // Supprime tous les enfants associés à un parent (cascade configurée)
    
    // Supprimer tous les enfants d'un enseignant
    void deleteByTeacherId(Long teacherId);  // Supprime tous les enfants associés à un enseignant (désassignation)
    
    // Supprimer tous les enfants d'un psychologue
    void deleteByPsychologistId(Long psychologistId);  // Supprime tous les enfants associés à un psychologue (désassignation)

}