package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.fasterxml.jackson.databind.ObjectMapper;  // Importe le mapper JSON (non utilisé dans ce service)
import com.kidcare.insight.dto.NoteRequest;  // Importe le DTO pour la requête de création de note
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour l'enfant concerné
import com.kidcare.insight.entity.PsychologistNote;  // Importe l'entité PsychologistNote pour la gestion des notes
import com.kidcare.insight.entity.User;  // Importe l'entité User pour le psychologue auteur
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.kidcare.insight.repository.PsychologistNoteRepository;  // Importe le repository pour les notes
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.slf4j.Logger;  // Importe l'interface de logging SLF4J
import org.slf4j.LoggerFactory;  // Importe la fabrique pour créer des loggers
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDate;  // Importe la classe LocalDate pour la date de session
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date de création
import java.util.ArrayList;  // Importe la classe ArrayList pour les listes vides
import java.util.List;  // Importe l'interface List pour les collections

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class PsychologistNoteService {  // Déclare le service responsable des notes des psychologues

    private static final Logger logger = LoggerFactory.getLogger(PsychologistNoteService.class);  // Crée un logger pour tracer l'exécution

    @Autowired  // Injecte automatiquement le repository PsychologistNoteRepository
    private PsychologistNoteRepository psychologistNoteRepository;  // Repository pour les notes des psychologues

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour les enfants

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour les utilisateurs

    @Autowired  // Injecte automatiquement le service NotificationService
    private NotificationService notificationService;  // Service pour envoyer des notifications (ex: au parent)

    // Méthode pour récupérer toutes les notes d'un enfant (avec gestion d'erreur robuste)
    public List<PsychologistNote> getNotesForChild(Long childId, String email) {
        System.out.println("🔵 getNotesForChild - childId: " + childId);  // Log de début
        
        try {  // Bloc try-catch pour capturer les erreurs
            User user = userRepository.findByEmail(email).orElse(null);  // Cherche l'utilisateur par email (ou null)
            if (user == null) {  // Si l'utilisateur n'existe pas
                System.out.println("⚠️ Utilisateur non trouvé: " + email);  // Log d'avertissement
                return new ArrayList<>();  // Retourne une liste vide (pas d'erreur)
            }

            Child child = childRepository.findById(childId).orElse(null);  // Cherche l'enfant par ID (ou null)
            if (child == null) {  // Si l'enfant n'existe pas
                System.out.println("⚠️ Enfant non trouvé: " + childId);  // Log d'avertissement
                return new ArrayList<>();  // Retourne une liste vide
            }

            // Récupère toutes les notes de l'enfant triées par date de session décroissante
            List<PsychologistNote> notes = psychologistNoteRepository.findByChildIdOrderBySessionDateDesc(childId);
            System.out.println("✅ " + notes.size() + " notes trouvées");  // Log du nombre de notes
            return notes;  // Retourne la liste des notes
            
        } catch (Exception e) {  // Capture toute exception
            System.err.println(" Erreur: " + e.getMessage());  // Log d'erreur
            e.printStackTrace();  // Affiche la trace complète
            return new ArrayList<>();  // Retourne une liste vide (fallback silencieux)
        }
    }

    @Transactional  // Exécute dans une transaction (annule en cas d'erreur)
    public PsychologistNote addNote(Long childId, NoteRequest req, String email) {  // Ajoute une nouvelle note psychologique
        System.out.println("🔵 addNote - childId: " + childId + ", email: " + email);  // Log de début
        
        // Récupération de l'utilisateur (psychologue)
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));  // Lance exception si non trouvé

        // Récupération de l'enfant
        Child child = childRepository.findById(childId)  // Cherche l'enfant par ID
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé avec ID: " + childId));  // Lance exception si non trouvé

        // Vérification que l'utilisateur est bien un psychologue
        if (!user.isPsychologist()) {  // Vérifie le rôle
            throw new RuntimeException("Seul un psychologue peut ajouter des notes");  // Lance exception si non psychologue
        }

        // Vérification qu'un psychologue est assigné à l'enfant
        if (child.getPsychologist() == null) {  // Vérifie si l'enfant a un psychologue assigné
            throw new RuntimeException("Aucun psychologue n'est assigné à cet enfant");  // Lance exception
        }

        // Vérification que l'utilisateur est bien le psychologue assigné à l'enfant
        if (!child.getPsychologist().getId().equals(user.getId())) {  // Compare l'ID du psychologue assigné avec celui de l'utilisateur
            throw new RuntimeException("Vous n'êtes pas le psychologue assigné à cet enfant");  // Lance exception
        }

        // Création et remplissage de la note
        PsychologistNote note = new PsychologistNote();  // Crée une nouvelle instance
        note.setChild(child);  // Associe l'enfant
        note.setPsychologist(user);  // Associe le psychologue auteur
        note.setNote(req.getNote());  // Définit le contenu de la note
        note.setSessionDate(req.getSessionDate() != null ? req.getSessionDate() : LocalDate.now());  // Définit la date de session (aujourd'hui si non fournie)
        note.setCreatedAt(LocalDateTime.now());  // Définit la date de création (saisie)

        PsychologistNote savedNote = psychologistNoteRepository.save(note);  // Sauvegarde la note en base
        System.out.println("✅ Note sauvegardée - ID: " + savedNote.getId());  // Log de confirmation

        // Envoi d'une notification au parent
        try {
            notificationService.notifyNoteAdded(child, user, req.getNote(), savedNote.getId());  // Appelle le service de notification
            System.out.println("✅ Notification envoyée au parent pour la note");  // Log de confirmation
        } catch (Exception e) {  // Capture les erreurs de notification (n'empêche pas la création de la note)
            logger.error("Erreur lors de l'envoi de la notification pour la note: {}", e.getMessage());  // Log d'erreur
        }
        
        return savedNote;  // Retourne la note sauvegardée
    }

    @Transactional  // Exécute dans une transaction
    public void deleteNote(Long noteId, String email) {  // Supprime une note (seul le psychologue auteur peut)
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        PsychologistNote note = psychologistNoteRepository.findById(noteId)  // Cherche la note par ID
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));  // Lance exception si non trouvée

        // Vérification que l'utilisateur est bien l'auteur de la note
        if (!note.getPsychologist().getId().equals(user.getId())) {  // Compare l'ID du psychologue auteur avec celui de l'utilisateur
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette note");  // Lance exception
        }

        psychologistNoteRepository.delete(note);  // Supprime la note
        System.out.println("✅ Note supprimée - ID: " + noteId);  // Log de confirmation
    }
}