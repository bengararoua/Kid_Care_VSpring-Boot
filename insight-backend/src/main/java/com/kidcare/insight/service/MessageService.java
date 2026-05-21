package com.kidcare.insight.service;

import com.kidcare.insight.entity.Message;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.MessageRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        msg.setIsRead(false);
        return messageRepository.save(msg);
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();
        return messageRepository.findConversation(user1, user2);
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }

    public void markAsRead(Long messageId, Long currentUserId) {
        Message msg = messageRepository.findById(messageId).orElseThrow();
        if (msg.getReceiver().getId().equals(currentUserId)) {
            msg.setIsRead(true);
            messageRepository.save(msg);
        }
    }

    public List<User> getContacts(Long currentUserId, String email) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        if (currentUser.isParent()) {
            return userRepository.findByRole(User.Role.TEACHER);
        } else if (currentUser.isTeacher()) {
            return userRepository.findByRole(User.Role.PARENT);
        } else {
            return userRepository.findByRole(User.Role.PARENT);
        }
    }
}