package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.User;  // Importe l'entité User pour la gestion des utilisateurs
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.security.core.authority.SimpleGrantedAuthority;  // Importe la classe pour créer une autorité simple (rôle)
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface UserDetails pour Spring Security
import org.springframework.security.core.userdetails.UserDetailsService;  // Importe l'interface UserDetailsService pour l'authentification
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // Importe l'exception pour utilisateur non trouvé
import org.springframework.security.crypto.password.PasswordEncoder;  // Importe l'encodeur de mots de passe (BCrypt)
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import java.util.Collections;  // Importe la classe Collections pour créer une liste immuable
import java.util.List;  // Importe l'interface List pour les collections

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class UserService implements UserDetailsService {  // Déclare le service utilisateur qui implémente UserDetailsService pour Spring Security

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer et sauvegarder les utilisateurs
    
    @Autowired  // Injecte automatiquement l'encodeur de mots de passe
    private PasswordEncoder passwordEncoder;  // Encodeur BCrypt pour hacher les mots de passe

    // ===== MÉTHODE OBLIGATOIRE DE UserDetailsService POUR SPRING SECURITY =====

    @Override  // Surcharge de la méthode de l'interface UserDetailsService
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {  // Charge un utilisateur par son email pour l'authentification
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email dans la base
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));  // Lance exception si non trouvé
        
        // Retourne un objet UserDetails que Spring Security utilise pour l'authentification
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),  // Utilise l'email comme identifiant
            user.getPassword(),  // Utilise le mot de passe hashé stocké
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))  // Crée une autorité avec le rôle (ex: ROLE_PARENT)
        );
    }
    
    // ===== MÉTHODES DE GESTION DES UTILISATEURS =====

    // Méthode pour enregistrer un nouvel utilisateur (inscription)
    public User register(String name, String email, String password, String role) {
        User user = new User();  // Crée une nouvelle instance d'utilisateur
        user.setName(name);  // Définit le nom
        user.setEmail(email);  // Définit l'email
        user.setPassword(passwordEncoder.encode(password));  // Encode le mot de passe avec BCrypt avant stockage
        user.setRole(role);  // Définit le rôle (parent, teacher, psychologist)
        user.setPoints(0);  // Initialise les points de gamification à 0
        return userRepository.save(user);  // Sauvegarde l'utilisateur en base et le retourne
    }
    
    // Méthode pour récupérer tous les utilisateurs d'un rôle spécifique
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);  // Appelle la méthode dérivée du repository
    }
    
    // Méthode pour trouver un utilisateur par son email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);  // Retourne l'utilisateur ou null si non trouvé
    }
    
    // Méthode pour trouver un utilisateur par son ID
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);  // Retourne l'utilisateur ou null si non trouvé
    }
}