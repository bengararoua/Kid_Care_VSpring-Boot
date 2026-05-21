package com.kidcare.insight.repository;

import com.kidcare.insight.entity.Message;
import com.kidcare.insight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Trouver la conversation entre deux utilisateurs
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);
    
    // Compter les messages non lus pour un utilisateur
    long countByReceiverAndIsReadFalse(User receiver);
    
    // Trouver tous les messages d'un utilisateur (envoyés ou reçus) triés par date
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.createdAt DESC")
    List<Message> findAllByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    // Trouver les messages non lus pour un utilisateur
    List<Message> findByReceiverAndIsReadFalse(User receiver);
    
    // Trouver les derniers messages pour chaque conversation
    @Query("SELECT m FROM Message m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM Message m2 WHERE m2.sender = :user OR m2.receiver = :user GROUP BY " +
           "CASE WHEN m2.sender = :user THEN m2.receiver ELSE m2.sender END)")
    List<Message> findLatestMessagesForUser(@Param("user") User user);
}