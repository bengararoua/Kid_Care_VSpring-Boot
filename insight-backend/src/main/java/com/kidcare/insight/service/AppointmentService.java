package com.kidcare.insight.service;

import com.kidcare.insight.entity.Appointment;
import com.kidcare.insight.entity.Message;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.AppointmentRepository;
import com.kidcare.insight.repository.MessageRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    public List<Appointment> getAppointmentsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentRepository.findByUser(user);
    }

    @Transactional
    public Appointment createAppointment(Map<String, Object> data, String email) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long receiverId = ((Number) data.get("receiver_id")).longValue();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Appointment appointment = new Appointment();
        appointment.setSender(sender);
        appointment.setReceiver(receiver);
        appointment.setTitle((String) data.getOrDefault("title", "Rendez-vous"));
        appointment.setScheduledAt(LocalDateTime.parse((String) data.get("scheduled_at")));
        appointment.setDuration((Integer) data.getOrDefault("duration", 30));
        appointment.setLocation((String) data.get("location"));
        appointment.setNotes((String) data.get("notes"));
        appointment.setType((String) data.getOrDefault("type", "video"));
        appointment.setStatus("pending");

        Appointment saved = appointmentRepository.save(appointment);

        // Créer un message de notification
        String messageContent = formatAppointmentMessage(saved);
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageContent);
        message.setIsRead(false);
        messageRepository.save(message);

        return saved;
    }

    public Appointment getAppointmentById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getSender().getId().equals(user.getId()) &&
                !appointment.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return appointment;
    }

    @Transactional
    public Appointment updateStatus(Long id, String status, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getSender().getId().equals(user.getId()) &&
                !appointment.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String oldStatus = appointment.getStatus();
        appointment.setStatus(status);

        // Envoyer un message de notification si le statut change
        if (!oldStatus.equals(status)) {
            User otherParty = appointment.getSender().getId().equals(user.getId())
                    ? appointment.getReceiver() : appointment.getSender();

            String statusMessage = getStatusMessage(status, appointment);
            Message message = new Message();
            message.setSender(user);
            message.setReceiver(otherParty);
            message.setContent(statusMessage);
            message.setIsRead(false);
            messageRepository.save(message);
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can delete an appointment");
        }

        appointmentRepository.delete(appointment);
    }

    private String formatAppointmentMessage(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String typeEmoji = switch (appointment.getType()) {
            case "video" -> "📹";
            case "phone" -> "📞";
            default -> "🏢";
        };

        return String.format(
                "📅 **NOUVEAU RENDEZ-VOUS**\n\n" +
                        "**Titre:** %s\n" +
                        "**Type:** %s %s\n" +
                        "**Date:** %s\n" +
                        "**Heure:** %s\n" +
                        "**Durée:** %d minutes\n" +
                        (appointment.getLocation() != null ? "**Lieu:** %s\n" : "") +
                        (appointment.getNotes() != null ? "\n**Notes:**\n%s" : ""),
                appointment.getTitle(),
                typeEmoji, appointment.getType(),
                appointment.getScheduledAt().format(dateFormatter),
                appointment.getScheduledAt().format(timeFormatter),
                appointment.getDuration(),
                appointment.getLocation() != null ? appointment.getLocation() : "",
                appointment.getNotes() != null ? appointment.getNotes() : ""
        );
    }

    private String getStatusMessage(String status, Appointment appointment) {
        return switch (status) {
            case "confirmed" -> "✅ Le rendez-vous du " +
                    appointment.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                    " a été confirmé";
            case "cancelled" -> "❌ Le rendez-vous du " +
                    appointment.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                    " a été annulé";
            case "completed" -> "✓ Le rendez-vous du " +
                    appointment.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                    " est terminé";
            default -> "Le statut du rendez-vous a été modifié";
        };
    }
}