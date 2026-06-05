package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.User;  // Importe l'entité User pour la gestion des utilisateurs
import com.kidcare.insight.repository.UserRepository;  // Importe le repository pour les utilisateurs
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.security.crypto.password.PasswordEncoder;  // Importe l'encodeur de mots de passe (BCrypt)
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring
import java.time.LocalDateTime;  // Importe la classe LocalDateTime pour la gestion des dates d'expiration
import java.util.Map;  // Importe l'interface Map pour le stockage temporaire des tokens
import java.util.UUID;  // Importe la classe UUID pour générer des tokens aléatoires uniques
import java.util.concurrent.ConcurrentHashMap;  // Importe la map thread-safe pour le stockage concurrent des tokens

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class PasswordResetService {  // Déclare le service responsable de la réinitialisation des mots de passe oubliés

    @Autowired  // Injecte automatiquement le repository UserRepository
    private UserRepository userRepository;  // Repository pour récupérer et sauvegarder les utilisateurs

    @Autowired  // Injecte automatiquement l'encodeur de mots de passe
    private PasswordEncoder passwordEncoder;  // Encodeur BCrypt pour hacher les nouveaux mots de passe

    // Map thread-safe pour stocker les tokens temporaires en mémoire (clé = token, valeur = ResetToken)
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    // Méthode pour créer un token de réinitialisation pour un email donné
    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);  // Cherche l'utilisateur par email (ou null si non trouvé)
        if (user == null) return null;  // Si l'utilisateur n'existe pas, retourne null

        // Supprimer les anciens tokens associés à cet email (pour éviter d'avoir plusieurs tokens actifs)
        resetTokens.entrySet().removeIf(entry -> entry.getValue().email.equals(email));  // Parcourt et supprime les entrées correspondant à l'email

        String token = UUID.randomUUID().toString();  // Génère un token aléatoire unique (ex: "550e8400-e29b-41d4-a716-446655440000")
        // Stocke le token avec l'email et une expiration dans 1 heure
        resetTokens.put(token, new ResetToken(email, LocalDateTime.now().plusHours(1)));  // Ajoute le token à la map
        return token;  // Retourne le token généré
    }

    // Méthode pour valider qu'un token est valide (non expiré et correspond à l'email)
    public boolean validateToken(String token, String email) {
        ResetToken rt = resetTokens.get(token);  // Récupère le ResetToken associé au token
        if (rt == null) return false;  // Si le token n'existe pas, retourne false
        if (!rt.email.equals(email)) return false;  // Si l'email ne correspond pas, retourne false
        if (LocalDateTime.now().isAfter(rt.expiry)) {  // Si la date actuelle est après la date d'expiration
            resetTokens.remove(token);  // Supprime le token expiré de la map
            return false;  // Retourne false
        }
        return true;  // Le token est valide (existe, email correspond, non expiré)
    }

    // Méthode pour réinitialiser le mot de passe avec un token valide et un nouveau mot de passe
    public boolean resetPassword(String token, String email, String newPassword) {
        if (!validateToken(token, email)) return false;  // Valide le token (s'il est invalide, retourne false)

        User user = userRepository.findByEmail(email).orElse(null);  // Cherche l'utilisateur par email (ou null)
        if (user == null) return false;  // Si l'utilisateur n'existe pas, retourne false

        user.setPassword(passwordEncoder.encode(newPassword));  // Encode le nouveau mot de passe avec BCrypt et le définit
        userRepository.save(user);  // Sauvegarde l'utilisateur modifié en base
        resetTokens.remove(token);  // Supprime le token (ne peut être utilisé qu'une seule fois)
        return true;  // Retourne true (succès)
    }

    // Classe interne privée pour stocker les informations d'un token
    private static class ResetToken {
        String email;  // Email de l'utilisateur associé au token
        LocalDateTime expiry;  // Date et heure d'expiration du token (1 heure après création)

        // Constructeur de ResetToken
        ResetToken(String email, LocalDateTime expiry) {
            this.email = email;  // Initialise l'email
            this.expiry = expiry;  // Initialise la date d'expiration
        }
    }
}