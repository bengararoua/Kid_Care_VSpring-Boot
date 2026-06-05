package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.dto.RecommendationRequest;  // Importe le DTO pour la requête de création de recommandation
import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour l'enfant concerné
import com.kidcare.insight.entity.Recommendation;  // Importe l'entité Recommendation pour la gestion des recommandations
import com.kidcare.insight.entity.User;  // Importe l'entité User pour l'auteur de la recommandation
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.kidcare.insight.repository.RecommendationRepository;  // Importe le repository pour les recommandations
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.slf4j.Logger;  // Importe l'interface de logging SLF4J
import org.slf4j.LoggerFactory;  // Importe la fabrique pour créer des loggers
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la date de création
import java.util.ArrayList;  // Importe la classe ArrayList pour les listes vides
import java.util.List;  // Importe l'interface List pour les collections

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class RecommendationService {  // Déclare le service responsable des recommandations personnalisées

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);  // Crée un logger pour tracer l'exécution

    @Autowired  // Injecte automatiquement le repository RecommendationRepository
    private RecommendationRepository recommendationRepository;  // Repository pour les recommandations

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour les enfants

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour les utilisateurs

    @Autowired  // Injecte automatiquement le service NotificationService
    private NotificationService notificationService;  // Service pour envoyer des notifications

    // ===== MÉTHODE DE LECTURE =====

    // Méthode pour récupérer toutes les recommandations d'un enfant (avec gestion d'erreur robuste)
    public List<Recommendation> getRecommendationsForChild(Long childId, String email) {
        System.out.println("🔵 getRecommendationsForChild - childId: " + childId);  // Log de début
        
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

            // Récupère toutes les recommandations de l'enfant triées par date de création décroissante
            List<Recommendation> recommendations = recommendationRepository.findByChildIdOrderByCreatedAtDesc(childId);
            System.out.println("✅ " + recommendations.size() + " recommandations trouvées");  // Log du nombre de recommandations
            return recommendations;  // Retourne la liste des recommandations
            
        } catch (Exception e) {  // Capture toute exception
            System.err.println(" Erreur: " + e.getMessage());  // Log d'erreur
            e.printStackTrace();  // Affiche la trace complète
            return new ArrayList<>();  // Retourne une liste vide (fallback silencieux)
        }
    }

    // ===== MÉTHODE DE CRÉATION =====

    @Transactional  // Exécute dans une transaction (annule en cas d'erreur)
    public Recommendation addRecommendation(Long childId, RecommendationRequest req, String email) {  // Ajoute une nouvelle recommandation
        System.out.println("🔵 addRecommendation - childId: " + childId + ", email: " + email);  // Log de début
        
        // Récupération de l'utilisateur (psychologue)
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));  // Lance exception si non trouvé

        // Récupération de l'enfant
        Child child = childRepository.findById(childId)  // Cherche l'enfant par ID
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé avec ID: " + childId));  // Lance exception si non trouvé

        // Vérification que l'utilisateur est bien un psychologue
        if (!user.isPsychologist()) {  // Vérifie le rôle
            throw new RuntimeException("Seul un psychologue peut ajouter des recommandations");  // Lance exception si non psychologue
        }

        // Vérification qu'un psychologue est assigné à l'enfant
        if (child.getPsychologist() == null) {  // Vérifie si l'enfant a un psychologue assigné
            throw new RuntimeException("Aucun psychologue n'est assigné à cet enfant");  // Lance exception
        }

        // Vérification que l'utilisateur est bien le psychologue assigné à l'enfant
        if (!child.getPsychologist().getId().equals(user.getId())) {  // Compare l'ID du psychologue assigné avec celui de l'utilisateur
            throw new RuntimeException("Vous n'êtes pas le psychologue assigné à cet enfant");  // Lance exception
        }

        // Création et remplissage de la recommandation
        Recommendation recommendation = new Recommendation();  // Crée une nouvelle instance
        recommendation.setChild(child);  // Associe l'enfant
        recommendation.setAuthor(user);  // Associe l'auteur (psychologue)
        recommendation.setTitle(req.getTitle());  // Définit le titre
        recommendation.setDescription(req.getDescription());  // Définit la description
        recommendation.setCategory(req.getCategory());  // Définit la catégorie (communication/social/attention/behavior)
        recommendation.setCompleted(false);  // Par défaut, non complétée
        recommendation.setCreatedAt(LocalDateTime.now());  // Définit la date de création

        Recommendation savedRecommendation = recommendationRepository.save(recommendation);  // Sauvegarde en base
        System.out.println("✅ Recommandation sauvegardée - ID: " + savedRecommendation.getId());  // Log de confirmation

        // Envoi d'une notification au parent
        try {
            notificationService.notifyRecommendationAdded(child, user, req.getTitle(), savedRecommendation.getId());  // Appelle le service de notification
            System.out.println("✅ Notification envoyée au parent pour la recommandation");  // Log de confirmation
        } catch (Exception e) {  // Capture les erreurs de notification (n'empêche pas la création)
            logger.error("Erreur lors de l'envoi de la notification pour la recommandation: {}", e.getMessage());  // Log d'erreur
        }
        
        return savedRecommendation;  // Retourne la recommandation sauvegardée
    }

    // ===== MÉTHODE DE TOGGLE (COMPLÉTION) =====

    @Transactional
    public Recommendation toggleRecommendation(Long id, String email) {  // Inverse l'état de complétion d'une recommandation
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        Recommendation recommendation = recommendationRepository.findById(id)  // Cherche la recommandation par ID
                .orElseThrow(() -> new RuntimeException("Recommandation non trouvée"));  // Lance exception si non trouvée

        Child child = recommendation.getChild();  // Récupère l'enfant associé
        boolean hasAccess = false;  // Initialise le flag d'accès à false
        
        // Vérification des droits d'accès selon le rôle de l'utilisateur
        if (user.isParent() && child.getParent() != null && child.getParent().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si parent de l'enfant
        } else if (user.isTeacher() && child.getTeacher() != null && child.getTeacher().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si enseignant de l'enfant
        } else if (user.isPsychologist() && child.getPsychologist() != null && child.getPsychologist().getId().equals(user.getId())) {
            hasAccess = true;  // Autorise si psychologue de l'enfant
        }

        if (!hasAccess) {  // Si non autorisé
            throw new RuntimeException("Accès non autorisé");  // Lance exception
        }

        // Inverse l'état de complétion
        boolean wasCompleted = recommendation.isCompleted();  // Sauvegarde l'ancien état
        recommendation.setCompleted(!wasCompleted);  // Inverse l'état (true→false ou false→true)
        Recommendation updated = recommendationRepository.save(recommendation);  // Sauvegarde
        
        //  Si la recommandation vient d'être complétée (et pas avant), notifier le psychologue
        try {
            if (!wasCompleted && updated.isCompleted()) {  // Si vient de passer de non-complété à complété
                notificationService.notifyRecommendationCompleted(child, user, updated.getTitle(), updated.getId());  // Notifie le psychologue
                System.out.println("✅ Notification envoyée au psychologue pour la complétion");  // Log de confirmation
            }
        } catch (Exception e) {  // Capture les erreurs de notification
            logger.error("Erreur lors de l'envoi de la notification pour la complétion: {}", e.getMessage());  // Log d'erreur
        }
        
        return updated;  // Retourne la recommandation mise à jour
    }

    // ===== MÉTHODE DE SUPPRESSION =====

    @Transactional
    public void deleteRecommendation(Long id, String email) {  // Supprime une recommandation
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        Recommendation recommendation = recommendationRepository.findById(id)  // Cherche la recommandation par ID
                .orElseThrow(() -> new RuntimeException("Recommandation non trouvée"));  // Lance exception si non trouvée

        // Vérification que l'utilisateur est l'auteur de la recommandation
        if (!recommendation.getAuthor().getId().equals(user.getId())) {  // Compare l'ID de l'auteur avec celui de l'utilisateur
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette recommandation");  // Lance exception
        }

        recommendationRepository.delete(recommendation);  // Supprime la recommandation
        System.out.println("✅ Recommandation supprimée - ID: " + id);  // Log de confirmation
    }
}