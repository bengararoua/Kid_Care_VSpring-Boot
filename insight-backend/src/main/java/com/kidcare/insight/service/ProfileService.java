package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.User;  // Importe l'entité User pour la gestion des utilisateurs
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.security.crypto.password.PasswordEncoder;  // Importe l'encodeur de mots de passe (BCrypt) pour vérifier/hacher
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import org.springframework.transaction.annotation.Transactional;  // Importe l'annotation pour gérer les transactions

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class ProfileService {  // Déclare le service responsable de la gestion du profil utilisateur

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer et sauvegarder les utilisateurs

    @Autowired  // Injecte automatiquement l'encodeur de mots de passe
    private PasswordEncoder passwordEncoder;  // Encodeur BCrypt pour vérifier et hacher les mots de passe

    // Méthode pour mettre à jour le nom et l'email du profil
    public User updateProfile(String email, String name, String newEmail) {
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par son email actuel
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        // Vérification que le nouvel email n'est pas déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(newEmail)) {  // Si le nouvel email est différent de l'email actuel
            boolean emailExists = userRepository.findByEmail(newEmail).isPresent();  // Vérifie si l'email existe déjà
            if (emailExists) {  // Si l'email existe déjà
                throw new RuntimeException("Cet email est déjà utilisé");  // Lance exception
            }
        }

        // Mise à jour des champs
        user.setName(name);  // Définit le nouveau nom
        user.setEmail(newEmail);  // Définit le nouvel email
        return userRepository.save(user);  // Sauvegarde et retourne l'utilisateur mis à jour
    }

    // Méthode pour changer le mot de passe (avec vérification de l'ancien)
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        // Vérification que l'ancien mot de passe est correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {  // Compare le mot de passe clair avec le hash stocké
            throw new RuntimeException("Le mot de passe actuel est incorrect");  // Lance exception si incorrect
        }

        // Validation de la robustesse du nouveau mot de passe
        if (newPassword == null || newPassword.length() < 6) {  // Vérifie que le nouveau mot de passe a au moins 6 caractères
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 6 caractères");  // Lance exception
        }

        // Hachage et sauvegarde du nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));  // Encode le nouveau mot de passe (BCrypt)
        userRepository.save(user);  // Sauvegarde l'utilisateur modifié
    }

    @Transactional  // Exécute dans une transaction (annule en cas d'erreur)
    public void deleteAccount(String email, String password) {  // Supprime complètement le compte utilisateur
        User user = userRepository.findByEmail(email)  // Cherche l'utilisateur par email
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé

        // Vérification du mot de passe avant suppression (sécurité)
        if (!passwordEncoder.matches(password, user.getPassword())) {  // Compare le mot de passe fourni avec le hash stocké
            throw new RuntimeException("Mot de passe incorrect");  // Lance exception si incorrect
        }

        //  Nettoyer toutes les relations avant suppression (éviter les contraintes de clé étrangère)
        user.getNotifications().clear();  // Vide la liste des notifications (dissocie)
        user.getSentMessages().clear();  // Vide la liste des messages envoyés
        user.getReceivedMessages().clear();  // Vide la liste des messages reçus
        user.getSentAppointments().clear();  // Vide la liste des rendez-vous envoyés
        user.getReceivedAppointments().clear();  // Vide la liste des rendez-vous reçus

        userRepository.save(user);  // Sauvegarde les modifications (nettoyage des relations)
        userRepository.delete(user);  // Supprime l'utilisateur de la base de données
    }
}