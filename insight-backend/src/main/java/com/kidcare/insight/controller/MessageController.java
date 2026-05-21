package com.kidcare.insight.controller;

import com.kidcare.insight.dto.MessageRequest;
import com.kidcare.insight.entity.Message;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.MessageRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/contacts")
    public ResponseEntity<List<Map<String, Object>>> getContacts(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<User> contacts = new ArrayList<>();
        
        if (currentUser.isParent()) {
            contacts.addAll(userRepository.findByRole(User.Role.TEACHER));
            contacts.addAll(userRepository.findByRole(User.Role.PSYCHOLOGIST));
        } else if (currentUser.isTeacher()) {
            contacts.addAll(userRepository.findByRole(User.Role.PARENT));
            contacts.addAll(userRepository.findByRole(User.Role.PSYCHOLOGIST));
        } else if (currentUser.isPsychologist()) {
            contacts.addAll(userRepository.findByRole(User.Role.PARENT));
            contacts.addAll(userRepository.findByRole(User.Role.TEACHER));
        }
        
        // Éviter les doublons
        contacts = contacts.stream().distinct().collect(java.util.stream.Collectors.toList());
        
        List<Map<String, Object>> response = new ArrayList<>();
        for (User contact : contacts) {
            Map<String, Object> c = new HashMap<>();
            c.put("id", contact.getId());
            c.put("name", contact.getName());
            c.put("email", contact.getEmail());
            c.put("role", contact.getRole().toString().toLowerCase());
            
            // Dernier message
            List<Message> lastMessages = messageRepository.findConversation(currentUser, contact);
            if (!lastMessages.isEmpty()) {
                Message last = lastMessages.get(lastMessages.size() - 1);
                c.put("last_message", last.getContent());
                c.put("last_message_date", last.getCreatedAt());
                c.put("unread_count", messageRepository.countByReceiverAndIsReadFalse(currentUser));
            } else {
                c.put("last_message", null);
                c.put("unread_count", 0);
            }
            response.add(c);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Map<String, Object>> conversations = new ArrayList<>();
        
        // Récupérer tous les messages triés par date
        List<Message> allMessages = messageRepository.findAll().stream()
            .filter(m -> m.getSender().getId().equals(currentUser.getId()) || m.getReceiver().getId().equals(currentUser.getId()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
        
        // Grouper par utilisateur
        Map<Long, Map<String, Object>> conversationMap = new HashMap<>();
        for (Message msg : allMessages) {
            Long otherId = msg.getSender().getId().equals(currentUser.getId()) ? msg.getReceiver().getId() : msg.getSender().getId();
            if (!conversationMap.containsKey(otherId)) {
                User other = userRepository.findById(otherId).orElseThrow();
                Map<String, Object> conv = new HashMap<>();
                conv.put("id", other.getId());
                conv.put("name", other.getName());
                conv.put("role", other.getRole().toString().toLowerCase());
                conv.put("last_message", msg.getContent());
                conv.put("last_message_date", msg.getCreatedAt());
                conv.put("unread_count", messageRepository.countByReceiverAndIsReadFalse(currentUser));
                conversationMap.put(otherId, conv);
            }
        }
        
        conversations.addAll(conversationMap.values());
        conversations.sort((a, b) -> ((Date)b.get("last_message_date")).compareTo((Date)a.get("last_message_date")));
        
        Map<String, Object> response = new HashMap<>();
        response.put("conversations", conversations);
        response.put("unreadCount", messageRepository.countByReceiverAndIsReadFalse(currentUser));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getConversation(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        User otherUser = userRepository.findById(userId).orElseThrow();
        List<Message> messages = messageRepository.findConversation(currentUser, otherUser);
        
        // Marquer les messages comme lus
        for (Message msg : messages) {
            if (msg.getReceiver().getId().equals(currentUser.getId()) && !msg.getIsRead()) {
                msg.setIsRead(true);
                messageRepository.save(msg);
            }
        }
        
        List<Map<String, Object>> response = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("sender_id", msg.getSender().getId());
            m.put("receiver_id", msg.getReceiver().getId());
            m.put("content", msg.getContent());
            m.put("is_read", msg.getIsRead());
            m.put("created_at", msg.getCreatedAt());
            response.add(m);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<Map<String, Object>> sendMessage(@PathVariable Long userId, @RequestBody MessageRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        User receiver = userRepository.findById(userId).orElseThrow();
        
        Message message = new Message();
        message.setSender(currentUser);
        message.setReceiver(receiver);
        message.setContent(req.getContent());
        message.setIsRead(false);
        message = messageRepository.save(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("sender_id", message.getSender().getId());
        response.put("receiver_id", message.getReceiver().getId());
        response.put("content", message.getContent());
        response.put("is_read", message.getIsRead());
        response.put("created_at", message.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        long count = messageRepository.countByReceiverAndIsReadFalse(currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }
}