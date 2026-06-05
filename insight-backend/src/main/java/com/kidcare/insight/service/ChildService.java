package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.dto.ChildRequest;  // Importe le DTO pour la requête de création/modification d'enfant
import com.kidcare.insight.entity.Child;  // Importe l'entité Child (enfant)
import com.kidcare.insight.entity.User;  // Importe l'entité User (utilisateur)
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions

import java.util.List;  // Importe l'interface List pour les collections

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class ChildService {  // Déclare le service responsable de la gestion des enfants

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour les enfants

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour les utilisateurs

    // Méthode pour récupérer tous les enfants associés à un utilisateur (quel que soit son rôle)
    public List<Child> getChildrenForUser(String email) {
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        String role = user.getRole().toString();  // Récupère le rôle de l'utilisateur (PARENT, TEACHER, PSYCHOLOGIST)

        System.out.println("📋 getChildrenForUser - User ID: " + user.getId() + ", Role: " + role);  // Log de débogage

        // Utiliser la nouvelle méthode findChildrenByUserId pour plus de sécurité
        // Cela fonctionne pour tous les rôles (PARENT, TEACHER, PSYCHOLOGIST)
        // La requête JPQL vérifie si l'utilisateur est parent, enseignant OU psychologue de l'enfant
        List<Child> children = childRepository.findChildrenByUserId(user.getId());
        
        System.out.println("   Enfants trouvés: " + children.size());  // Log du nombre d'enfants trouvés
        return children;  // Retourne la liste des enfants
    }

    @Transactional  // Exécute la méthode dans une transaction (annule en cas d'erreur)
    public Child createChild(ChildRequest req, String email) {  // Crée un nouvel enfant
        User currentUser = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        //  Seul un parent peut créer un enfant (sécurité)
        if (!"PARENT".equals(currentUser.getRole().toString())) {  // Vérifie que l'utilisateur a le rôle PARENT
            throw new RuntimeException("Seuls les parents peuvent créer des enfants");  // Lance exception si non parent
        }

        Child child = new Child();  // Crée une nouvelle instance d'enfant
        child.setName(req.getName());  // Définit le nom de l'enfant
        child.setAge(req.getAge());  // Définit l'âge de l'enfant
        child.setNotes(req.getNotes());  // Définit les notes

        // Associer automatiquement l'enfant au parent connecté
        child.setParent(currentUser);  // Le parent est l'utilisateur connecté

        // Si un psychologue est spécifié dans la requête, l'ajouter
        if (req.getPsychologistId() != null) {  // Vérifie si un ID psychologue a été fourni
            User psychologist = userRepository.findById(req.getPsychologistId())  // Cherche le psychologue par ID
                    .orElseThrow(() -> new RuntimeException("Psychologue non trouvé"));  // Lance exception si non trouvé
            // Vérifier que le psychologue a bien le rôle PSYCHOLOGIST
            if (!"PSYCHOLOGIST".equals(psychologist.getRole().toString())) {  // Vérifie le rôle
                throw new RuntimeException("L'utilisateur sélectionné n'est pas un psychologue");  // Lance exception si mauvais rôle
            }
            child.setPsychologist(psychologist);  // Associe le psychologue à l'enfant
        }

        // Si un enseignant est spécifié dans la requête, l'ajouter
        if (req.getTeacherId() != null) {  // Vérifie si un ID enseignant a été fourni
            User teacher = userRepository.findById(req.getTeacherId())  // Cherche l'enseignant par ID
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));  // Lance exception si non trouvé
            // Vérifier que l'enseignant a bien le rôle TEACHER
            if (!"TEACHER".equals(teacher.getRole().toString())) {  // Vérifie le rôle
                throw new RuntimeException("L'utilisateur sélectionné n'est pas un enseignant");  // Lance exception si mauvais rôle
            }
            child.setTeacher(teacher);  // Associe l'enseignant à l'enfant
        }

        System.out.println("✅ Enfant créé - ID: " + child.getId() + ", Nom: " + child.getName() + ", Parent: " + currentUser.getEmail());  // Log de confirmation

        return childRepository.save(child);  // Sauvegarde l'enfant en base et le retourne
    }

    // Méthode pour récupérer un enfant par son ID (avec vérification des droits d'accès)
    public Child getChildById(Long id, String email) {
        User currentUser = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));  // Lance exception si non trouvé

        //  Utiliser la méthode sécurisée qui vérifie l'accès (parent, teacher ou psychologist)
        Child child = childRepository.findByIdAndUserAccess(id, currentUser.getId());
        
        if (child == null) {  // Si aucun enfant n'est trouvé (ou accès refusé)
            System.out.println(" ACCÈS REFUSÉ - User: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ") n'a pas accès à l'enfant ID: " + id);  // Log d'avertissement
            throw new RuntimeException("Accès non autorisé à cet enfant");  // Lance exception
        }

        System.out.println("✅ ACCÈS AUTORISÉ - User: " + currentUser.getEmail() + " a accès à l'enfant: " + child.getName());  // Log de confirmation
        return child;  // Retourne l'enfant
    }

    @Transactional  // Exécute la méthode dans une transaction
    public Child updateChild(Long id, ChildRequest req, String email) {  // Met à jour un enfant existant
        User currentUser = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));  // Lance exception si non trouvé
        
        // Vérifier d'abord l'accès à l'enfant (parent, teacher ou psychologist)
        Child child = childRepository.findByIdAndUserAccess(id, currentUser.getId());
        
        if (child == null) {  // Si aucun enfant n'est trouvé (ou accès refusé)
            throw new RuntimeException("Accès non autorisé - Vous ne pouvez pas modifier cet enfant");  // Lance exception
        }
        
        //  Seul le parent ou un admin peut modifier les informations de base de l'enfant
        String role = currentUser.getRole().toString();  // Récupère le rôle de l'utilisateur
        boolean isParent = child.getParent() != null && child.getParent().getId().equals(currentUser.getId());  // Vérifie si c'est le parent
        boolean isAdmin = "ADMIN".equals(role);  // Vérifie si c'est un administrateur
        
        if (!isParent && !isAdmin) {  // Si ni parent ni admin
            throw new RuntimeException("Seul le parent peut modifier les informations de l'enfant");  // Lance exception
        }

        // Mise à jour des champs uniquement s'ils sont fournis dans la requête
        if (req.getName() != null) child.setName(req.getName());  // Met à jour le nom
        if (req.getAge() != null) child.setAge(req.getAge());  // Met à jour l'âge
        if (req.getNotes() != null) child.setNotes(req.getNotes());  // Met à jour les notes

        // Seul un admin peut modifier les assignations (psychologue/enseignant)
        if (isAdmin) {  // Si l'utilisateur est administrateur
            if (req.getPsychologistId() != null) {  // Si un psychologue est spécifié
                User psychologist = userRepository.findById(req.getPsychologistId())  // Cherche le psychologue
                        .orElseThrow(() -> new RuntimeException("Psychologue non trouvé"));  // Lance exception si non trouvé
                if (!"PSYCHOLOGIST".equals(psychologist.getRole().toString())) {  // Vérifie le rôle
                    throw new RuntimeException("L'utilisateur sélectionné n'est pas un psychologue");  // Lance exception
                }
                child.setPsychologist(psychologist);  // Met à jour le psychologue
            }

            if (req.getTeacherId() != null) {  // Si un enseignant est spécifié
                User teacher = userRepository.findById(req.getTeacherId())  // Cherche l'enseignant
                        .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));  // Lance exception si non trouvé
                if (!"TEACHER".equals(teacher.getRole().toString())) {  // Vérifie le rôle
                    throw new RuntimeException("L'utilisateur sélectionné n'est pas un enseignant");  // Lance exception
                }
                child.setTeacher(teacher);  // Met à jour l'enseignant
            }
        }

        System.out.println("✅ Enfant modifié - ID: " + id + " par: " + currentUser.getEmail());  // Log de confirmation
        return childRepository.save(child);  // Sauvegarde et retourne l'enfant mis à jour
    }

    @Transactional  // Exécute la méthode dans une transaction
    public void deleteChild(Long id, String email) {  // Supprime un enfant
        User currentUser = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));  // Lance exception si non trouvé
        
        // Vérifier d'abord l'accès à l'enfant (parent, teacher ou psychologist)
        Child child = childRepository.findByIdAndUserAccess(id, currentUser.getId());
        
        if (child == null) {  // Si aucun enfant n'est trouvé (ou accès refusé)
            throw new RuntimeException("Accès non autorisé - Vous ne pouvez pas supprimer cet enfant");  // Lance exception
        }
        
        //  Seul le parent ou un admin peut supprimer un enfant
        boolean isParent = child.getParent() != null && child.getParent().getId().equals(currentUser.getId());  // Vérifie si c'est le parent
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().toString());  // Vérifie si c'est un administrateur
        
        if (!isParent && !isAdmin) {  // Si ni parent ni admin
            throw new RuntimeException("Seul le parent peut supprimer son enfant");  // Lance exception
        }
        
        childRepository.delete(child);  // Supprime l'enfant (cascade supprime les logs, routines, etc.)
        System.out.println("✅ Enfant supprimé - ID: " + id + " par: " + currentUser.getEmail());  // Log de confirmation
    }
}