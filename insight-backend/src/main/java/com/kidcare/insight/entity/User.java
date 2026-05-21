package com.kidcare.insight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role = Role.PARENT;
    private LocalDateTime createdAt;
    private String avatar;
    private Integer points = 0;
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Child> children;
    @OneToMany(mappedBy = "psychologist", fetch = FetchType.EAGER)
    private List<Child> assignedChildren;
    @OneToMany(mappedBy = "sender")
    private List<Message> sentMessages;
    @OneToMany(mappedBy = "receiver")
    private List<Message> receivedMessages;

    public enum Role { PARENT, TEACHER, PSYCHOLOGIST }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public List<Child> getChildren() { return children; }
    public void setChildren(List<Child> children) { this.children = children; }
    public List<Child> getAssignedChildren() { return assignedChildren; }
    public void setAssignedChildren(List<Child> assignedChildren) { this.assignedChildren = assignedChildren; }
    public List<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(List<Message> sentMessages) { this.sentMessages = sentMessages; }
    public List<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(List<Message> receivedMessages) { this.receivedMessages = receivedMessages; }

    public boolean isParent() { return role == Role.PARENT; }
    public boolean isTeacher() { return role == Role.TEACHER; }
    public boolean isPsychologist() { return role == Role.PSYCHOLOGIST; }
}