package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.Appointment;  // Importe l'entité Appointment pour la gestion des rendez-vous
import com.kidcare.insight.entity.Message;  // Importe l'entité Message pour envoyer des notifications par message
import com.kidcare.insight.entity.User;  // Importe l'entité User pour identifier les participants
import com.kidcare.insight.repository.AppointmentRepository;  // Importe le repository pour les rendez-vous
import com.kidcare.insight.repository.MessageRepository;  // Importe le repository pour les messages
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.scheduling.annotation.EnableScheduling;  // Importe l'annotation pour activer la planification de tâches
import org.springframework.scheduling.annotation.Scheduled;  // Importe l'annotation pour planifier une tâche à intervalles réguliers
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un composant Spring de type Service
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour les dates et heures
import java.time.format.DateTimeFormatter;  // Importe la classe DateTimeFormatter pour formater les dates
import java.util.List;  // Importe l'interface List pour les collections
import java.util.Map;  // Importe l'interface Map pour les données de création

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
@EnableScheduling  // Active la planification de tâches automatiques (ex: mise à jour des rendez-vous)
public class AppointmentService {  // Déclare le service responsable de la gestion des rendez-vous

    @Autowired  // Injecte automatiquement le repository AppointmentRepository
    private AppointmentRepository appointmentRepository;  // Repository pour sauvegarder et récupérer les rendez-vous

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer les utilisateurs

    @Autowired  // Injecte automatiquement le repository MessageRepository
    private MessageRepository messageRepository;  // Repository pour envoyer des messages de notification

    // Méthode publique pour récupérer tous les rendez-vous d'un utilisateur (met à jour les passés avant)
    public List<Appointment> getAppointmentsForUser(String email) {
        // Mettre à jour les rendez-vous passés avant d'afficher (les marque comme terminés)
        updatePastAppointmentsToCompleted();  // Appelle la méthode de mise à jour des rendez-vous dépassés
        
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé
        return appointmentRepository.findByUser(user);  // Retourne tous les rendez-vous de l'utilisateur (expéditeur ou destinataire)
    }

    @Transactional  // Exécute la méthode dans une transaction (annule en cas d'erreur)
    public Appointment createAppointment(Map<String, Object> data, String email) {  // Crée un nouveau rendez-vous
        User sender = userRepository.findByEmail(email)  // Cherche l'expéditeur (utilisateur connecté) par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        Long receiverId = ((Number) data.get("receiver_id")).longValue();  // Extrait l'ID du destinataire depuis les données
        User receiver = userRepository.findById(receiverId)  // Cherche le destinataire par son ID
                .orElseThrow(() -> new RuntimeException("Receiver not found"));  // Lance exception si non trouvé

        Appointment appointment = new Appointment();  // Crée une nouvelle instance de rendez-vous
        appointment.setSender(sender);  // Définit l'expéditeur
        appointment.setReceiver(receiver);  // Définit le destinataire
        appointment.setTitle((String) data.getOrDefault("title", "Rendez-vous"));  // Définit le titre (par défaut "Rendez-vous")
        appointment.setScheduledAt(LocalDateTime.parse((String) data.get("scheduled_at")));  // Définit la date et heure planifiées
        appointment.setDuration((Integer) data.getOrDefault("duration", 30));  // Définit la durée (par défaut 30 minutes)
        appointment.setLocation((String) data.get("location"));  // Définit le lieu
        appointment.setNotes((String) data.get("notes"));  // Définit les notes
        appointment.setType((String) data.getOrDefault("type", "video"));  // Définit le type (par défaut "video")
        appointment.setStatus("pending");  // Définit le statut initial comme "en attente"

        Appointment saved = appointmentRepository.save(appointment);  // Sauvegarde le rendez-vous en base

        // Créer un message de notification AVEC LE LIEN vers le rendez-vous
        String messageContent = formatAppointmentMessage(saved);  // Formate un message détaillé avec lien
        Message message = new Message();  // Crée un nouveau message
        message.setSender(sender);  // Définit l'expéditeur du message (celui qui crée le rendez-vous)
        message.setReceiver(receiver);  // Définit le destinataire du message (la personne invitée)
        message.setContent(messageContent);  // Définit le contenu du message (notification du rendez-vous)
        message.setIsRead(false);  // Marque le message comme non lu
        messageRepository.save(message);  // Sauvegarde le message de notification

        return saved;  // Retourne le rendez-vous créé
    }

    // Méthode publique pour récupérer un rendez-vous par son ID (avec vérification des droits)
    public Appointment getAppointmentById(Long id, String email) {
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        Appointment appointment = appointmentRepository.findById(id)  // Cherche le rendez-vous par ID
                .orElseThrow(() -> new RuntimeException("Appointment not found"));  // Lance exception si non trouvé

        // Vérifie que l'utilisateur est soit l'expéditeur, soit le destinataire du rendez-vous
        if (!appointment.getSender().getId().equals(user.getId()) &&
                !appointment.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");  // Lance exception si l'utilisateur n'est pas autorisé
        }

        return appointment;  // Retourne le rendez-vous
    }

    @Transactional  // Exécute la méthode dans une transaction
    public Appointment updateStatus(Long id, String status, String email) {  // Met à jour le statut d'un rendez-vous
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        Appointment appointment = appointmentRepository.findById(id)  // Cherche le rendez-vous par ID
                .orElseThrow(() -> new RuntimeException("Appointment not found"));  // Lance exception si non trouvé

        // Vérifie que l'utilisateur a le droit de modifier le statut (est participant)
        if (!appointment.getSender().getId().equals(user.getId()) &&
                !appointment.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");  // Lance exception si non autorisé
        }

        String oldStatus = appointment.getStatus();  // Sauvegarde l'ancien statut pour comparaison
        appointment.setStatus(status);  // Définit le nouveau statut

        // Envoyer un message de notification si le statut change (et non pas pour le même statut)
        if (!oldStatus.equals(status)) {
            User otherParty = appointment.getSender().getId().equals(user.getId())  // Détermine l'autre participant
                    ? appointment.getReceiver() : appointment.getSender();  // Si user est sender → other = receiver, sinon inverse

            String statusMessage = getStatusMessage(status, appointment);  // Formate un message selon le statut
            Message message = new Message();  // Crée un nouveau message
            message.setSender(user);  // Définit l'expéditeur (celui qui change le statut)
            message.setReceiver(otherParty);  // Définit le destinataire (l'autre participant)
            message.setContent(statusMessage);  // Définit le contenu (notification de changement)
            message.setIsRead(false);  // Marque comme non lu
            messageRepository.save(message);  // Sauvegarde le message
        }

        return appointmentRepository.save(appointment);  // Sauvegarde et retourne le rendez-vous mis à jour
    }

    @Transactional  // Exécute la méthode dans une transaction
    public void deleteAppointment(Long id, String email) {  // Supprime un rendez-vous (seul le créateur peut)
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("User not found"));  // Lance exception si non trouvé

        Appointment appointment = appointmentRepository.findById(id)  // Cherche le rendez-vous par ID
                .orElseThrow(() -> new RuntimeException("Appointment not found"));  // Lance exception si non trouvé

        // Seul le créateur (sender) peut supprimer le rendez-vous
        if (!appointment.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can delete an appointment");  // Lance exception si non créateur
        }

        appointmentRepository.delete(appointment);  // Supprime le rendez-vous de la base
    }

    // Méthode publique pour mettre à jour les rendez-vous passés en statut "completed"
    @Transactional
    public void updatePastAppointmentsToCompleted() {
        LocalDateTime now = LocalDateTime.now();  // Récupère la date et heure actuelles
        
        // Récupérer tous les rendez-vous passés qui ne sont ni terminés ni annulés
        List<Appointment> pastAppointments = appointmentRepository.findPastAppointmentsNotCompleted(now);
        
        int updatedCount = 0;  // Compteur de rendez-vous mis à jour
        for (Appointment appointment : pastAppointments) {  // Parcourt chaque rendez-vous passé
            String oldStatus = appointment.getStatus();  // Sauvegarde l'ancien statut (non utilisé mais conservé)
            appointment.setStatus("completed");  // Définit le statut comme "terminé"
            appointmentRepository.save(appointment);  // Sauvegarde la modification
            updatedCount++;  // Incrémente le compteur
            System.out.println("✅ Rendez-vous ID " + appointment.getId() + " marqué comme terminé (date passée)");  // Log de confirmation
        }
        
        if (updatedCount > 0) {  // Si au moins un rendez-vous a été mis à jour
            System.out.println("📅 " + updatedCount + " rendez-vous passés marqués comme terminés");  // Log récapitulatif
        }
    }
    
    // Exécute la vérification tous les jours à 1h du matin (planification automatique)
    @Scheduled(cron = "0 0 1 * * ?")  // Format cron : seconde minute heure jour mois jour_semaine → 0 0 1 = 1h00 chaque jour
    public void autoUpdatePastAppointments() {  // Méthode appelée automatiquement par le scheduler
        System.out.println("🕐 [Scheduler] Exécution de la vérification des rendez-vous passés...");  // Log de début
        updatePastAppointmentsToCompleted();  // Appelle la méthode de mise à jour
    }

    // Méthode privée pour formater le message de notification d'un nouveau rendez-vous (AVEC LIEN)
    private String formatAppointmentMessage(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");  // Formateur pour la date
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");  // Formateur pour l'heure

        // Émoji selon le type de rendez-vous (video, phone, ou autre)
        String typeEmoji = switch (appointment.getType()) {
            case "video" -> "📹";  // Vidéo → emoji caméra
            case "phone" -> "📞";  // Téléphone → emoji téléphone
            default -> "🏢";  // Autre → emoji bâtiment (présentiel)
        };

        // Construction du message avec tous les détails
        String message = String.format(
                "📅 **NOUVEAU RENDEZ-VOUS**\n\n" +  // Titre en gras
                "**Titre:** %s\n" +  // Titre du rendez-vous
                "**Type:** %s %s\n" +  // Émoji + type
                "**Date:** %s\n" +  // Date formatée
                "**Heure:** %s\n" +  // Heure formatée
                "**Durée:** %d minutes",  // Durée en minutes
                appointment.getTitle(),
                typeEmoji, appointment.getType(),
                appointment.getScheduledAt().format(dateFormatter),
                appointment.getScheduledAt().format(timeFormatter),
                appointment.getDuration()
        );

        // Ajouter le lieu si présent (non vide)
        if (appointment.getLocation() != null && !appointment.getLocation().isEmpty()) {
            message += "\n**Lieu:** " + appointment.getLocation();  // Ajoute le lieu au message
        }

        // Ajouter les notes si présentes
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            message += "\n\n**Notes:**\n" + appointment.getNotes();  // Ajoute les notes au message
        }

        // Ajouter le lien vers la page de détail du rendez-vous
        message += "\n\n🔗 **Lien:** /appointments/" + appointment.getId();  // Lien cliquable pour le frontend

        return message;  // Retourne le message complet
    }

    // Méthode privée pour formater le message de notification de changement de statut
    private String getStatusMessage(String status, Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");  // Formateur date/heure
        String baseMessage = switch (status) {  // Switch sur le nouveau statut
            case "confirmed" -> "✅ **Le rendez-vous** du " +  // Statut confirmé
                    appointment.getScheduledAt().format(formatter) +  // Date et heure du rendez-vous
                    " a été **confirmé**";  // Message de confirmation
            case "cancelled" -> " **Le rendez-vous** du " +  // Statut annulé
                    appointment.getScheduledAt().format(formatter) +  // Date et heure du rendez-vous
                    " a été **annulé**";  // Message d'annulation
            case "completed" -> "✓ **Le rendez-vous** du " +  // Statut terminé
                    appointment.getScheduledAt().format(formatter) +  // Date et heure du rendez-vous
                    " est **terminé**";  // Message de terminaison
            default -> "Le statut du rendez-vous a été modifié";  // Message générique
        };
        
        // Ajouter le lien aussi dans les messages de statut
        return baseMessage + "\n\n🔗 **Voir le rendez-vous:** /appointments/" + appointment.getId();  // Ajoute le lien
    }
}