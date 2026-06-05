package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.dto.RoutineDTO;  // Importe le DTO pour le transfert des données de routine
import com.kidcare.insight.entity.Routine;  // Importe l'entité Routine pour la gestion des routines
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour l'enfant concerné
import com.kidcare.insight.entity.User;  // Importe l'entité User pour le créateur de la routine
import com.kidcare.insight.repository.RoutineRepository;  // Importe le repository pour les routines
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions

import java.time.LocalTime;  // Importe la classe LocalTime pour l'heure de la routine
import java.util.List;  // Importe l'interface List pour les collections
import java.util.Map;  // Importe l'interface Map pour les données de création
import java.util.stream.Collectors;  // Importe la classe Collectors pour les streams

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class RoutineService {  // Déclare le service responsable des routines quotidiennes des enfants

    @Autowired  // Injecte automatiquement le repository RoutineRepository
    private RoutineRepository routineRepository;  // Repository pour les routines

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour les enfants

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour les utilisateurs

    // ===== MÉTHODE DE CONVERSION (Entité → DTO) =====

    // Méthode privée pour convertir une entité Routine en RoutineDTO (structure plate pour le frontend)
    private RoutineDTO toDTO(Routine routine) {
        RoutineDTO dto = new RoutineDTO();  // Crée un nouveau DTO
        dto.setId(routine.getId());  // Copie l'ID
        dto.setChildId(routine.getChild() != null ? routine.getChild().getId() : null);  // Copie l'ID de l'enfant
        dto.setDayOfWeek(routine.getDayOfWeek());  // Copie le jour de la semaine
        dto.setTime(routine.getTime() != null ? routine.getTime().toString() : null);  // Copie l'heure (convertie en String)
        dto.setActivity(routine.getActivity());  // Copie l'activité
        dto.setDuration(routine.getDuration());  // Copie la durée
        dto.setCompleted(routine.getCompleted());  // Copie l'état de complétion
        dto.setOrderIndex(routine.getOrderIndex());  // Copie l'ordre d'affichage
        dto.setCreatedBy(routine.getUser() != null ? routine.getUser().getName() : null);  // Copie le nom du créateur
        dto.setCreatedByRole(routine.getUser() != null ? routine.getUser().getRole() : null);  // Copie le rôle du créateur
        return dto;  // Retourne le DTO
    }

    // ===== MÉTHODE DE LECTURE (avec vérification d'accès) =====

    // Retourne TOUTES les routines de l'enfant (pas filtrées par utilisateur)
    public List<RoutineDTO> getRoutinesForChild(Long childId, String email) {
        // Vérifier que l'utilisateur existe et a accès à l'enfant
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        Child child = childRepository.findById(childId)  // Cherche l'enfant par ID
                .orElseThrow(() -> new RuntimeException("Child not found"));  // Lance exception si non trouvé
        
        //  Vérifier que l'utilisateur a le droit de voir cet enfant
        boolean hasAccess = false;  // Initialise le flag d'accès
        
        // Vérification selon le rôle de l'utilisateur
        if (child.getParent() != null && child.getParent().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si c'est le parent
        }
        else if (child.getTeacher() != null && child.getTeacher().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si c'est l'enseignant
        }
        else if (child.getPsychologist() != null && child.getPsychologist().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si c'est le psychologue
        }
        else if ("admin".equalsIgnoreCase(user.getRole())) {
            hasAccess = true;  // Autorise si c'est un administrateur
        }
        
        if (!hasAccess) {  // Si aucun accès n'est autorisé
            throw new RuntimeException("Unauthorized: You don't have access to this child");  // Lance exception
        }
        
        //  Retourner TOUTES les routines de l'enfant (pas filtré par user)
        List<Routine> routines = routineRepository.findByChild(child);  // Récupère toutes les routines de l'enfant
        
        // Logs de débogage
        System.out.println("=== DEBUG ROUTINES ===");
        System.out.println("Child ID: " + childId);
        System.out.println("User email: " + email + " (role: " + user.getRole() + ")");
        System.out.println("Routines trouvées en base: " + routines.size());
        for (Routine r : routines) {
            System.out.println("  - Routine: id=" + r.getId() + 
                             ", dayOfWeek=" + r.getDayOfWeek() + 
                             ", activity=" + r.getActivity() +
                             ", createdBy=" + (r.getUser() != null ? r.getUser().getEmail() : "unknown"));
        }
        
        // Conversion de chaque Routine en RoutineDTO et collecte dans une liste
        return routines.stream()
                .map(this::toDTO)  // Convertit chaque entité en DTO
                .collect(Collectors.toList());  // Collecte dans une liste
    }

    // ===== MÉTHODE DE CRÉATION =====

    @Transactional  // Exécute dans une transaction
    public RoutineDTO createRoutine(Long childId, Map<String, Object> data, String email) {  // Crée une nouvelle routine
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        Child child = childRepository.findById(childId)  // Cherche l'enfant par ID
                .orElseThrow(() -> new RuntimeException("Child not found"));  // Lance exception si non trouvé

        // Création et remplissage de la routine
        Routine routine = new Routine();  // Crée une nouvelle instance
        routine.setChild(child);  // Associe l'enfant
        routine.setUser(user);  //  Garde trace de qui a créé la routine
        routine.setDayOfWeek((String) data.get("day_of_week"));  // Définit le jour de la semaine
        routine.setTime(LocalTime.parse((String) data.get("time")));  // Définit l'heure (convertit String → LocalTime)
        routine.setActivity((String) data.get("activity"));  // Définit l'activité
        routine.setDuration(data.get("duration") != null ? (Integer) data.get("duration") : null);  // Définit la durée (si présente)
        routine.setCompleted(false);  // Par défaut, non complétée
        routine.setOrderIndex(0);  // Par défaut, ordre 0 (sera réorganisé manuellement)

        // Logs de débogage
        System.out.println("=== CREATE ROUTINE ===");
        System.out.println("Créée par: " + user.getEmail() + " (" + user.getRole() + ")");
        System.out.println("Pour enfant: " + child.getName());
        
        return toDTO(routineRepository.save(routine));  // Sauvegarde, convertit en DTO et retourne
    }

    // ===== MÉTHODE DE MISE À JOUR =====

    @Transactional
    public RoutineDTO updateRoutine(Long id, Map<String, Object> data, String email) {  // Met à jour une routine existante
        Routine routine = getRoutineById(id, email);  // Récupère la routine (avec vérification des droits)
        
        // Mise à jour conditionnelle des champs (seulement s'ils sont présents dans la requête)
        if (data.containsKey("day_of_week")) {  // Si un jour est fourni
            routine.setDayOfWeek((String) data.get("day_of_week"));  // Met à jour le jour
        }
        if (data.containsKey("time")) {  // Si une heure est fournie
            routine.setTime(LocalTime.parse((String) data.get("time")));  // Met à jour l'heure
        }
        if (data.containsKey("activity")) {  // Si une activité est fournie
            routine.setActivity((String) data.get("activity"));  // Met à jour l'activité
        }
        if (data.containsKey("duration")) {  // Si une durée est fournie
            routine.setDuration((Integer) data.get("duration"));  // Met à jour la durée
        }
        if (data.containsKey("completed")) {  // Si un état de complétion est fourni
            routine.setCompleted((Boolean) data.get("completed"));  // Met à jour l'état
        }

        return toDTO(routineRepository.save(routine));  // Sauvegarde, convertit et retourne
    }

    // ===== MÉTHODE DE TOGGLE (COMPLÉTION) =====

    @Transactional
    public RoutineDTO toggleComplete(Long id, String email) {  // Inverse l'état de complétion d'une routine
        Routine routine = getRoutineById(id, email);  // Récupère la routine (avec vérification des droits)
        routine.setCompleted(!routine.getCompleted());  // Inverse l'état (true→false ou false→true)
        return toDTO(routineRepository.save(routine));  // Sauvegarde, convertit et retourne
    }

    // ===== MÉTHODE DE SUPPRESSION =====

    @Transactional
    public void deleteRoutine(Long id, String email) {  // Supprime une routine
        Routine routine = getRoutineById(id, email);  // Récupère la routine (avec vérification des droits)
        routineRepository.delete(routine);  // Supprime la routine
    }

    // ===== MÉTHODE PRIVÉE DE VÉRIFICATION DES DROITS =====

    // Permettre la modification/suppression aux parents, teachers, psychologues et créateur
    private Routine getRoutineById(Long id, String email) {  // Récupère une routine et vérifie les droits d'accès
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        
        Routine routine = routineRepository.findById(id)  // Cherche la routine par ID
                .orElseThrow(() -> new RuntimeException("Routine not found"));  // Lance exception si non trouvée
        
        Child child = routine.getChild();  // Récupère l'enfant associé à la routine
        
        // Vérifier les droits d'accès (plusieurs conditions)
        boolean hasAccess = false;  // Initialise le flag d'accès
        
        // Condition 1 : Créateur original de la routine
        if (routine.getUser().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise le créateur
        }
        // Condition 2 : Parent de l'enfant
        else if (child.getParent() != null && child.getParent().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise le parent
        }
        // Condition 3 : Teacher de l'enfant
        else if (child.getTeacher() != null && child.getTeacher().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise l'enseignant
        }
        // Condition 4 : Psychologue de l'enfant
        else if (child.getPsychologist() != null && child.getPsychologist().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise le psychologue
        }
        // Condition 5 : Administrateur
        else if ("admin".equalsIgnoreCase(user.getRole())) {
            hasAccess = true;  // Autorise l'admin
        }
        
        if (!hasAccess) {  // Si aucun accès n'est autorisé
            throw new RuntimeException("Unauthorized: You don't have permission to modify this routine");  // Lance exception
        }

        return routine;  // Retourne la routine (accès autorisé)
    }
}