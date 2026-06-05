package com.kidcare.insight.config;  // Déclare le package auquel appartient cette classe utilitaire JWT

import io.jsonwebtoken.Claims;  // Importe l'interface représentant les claims (données) du token JWT
import io.jsonwebtoken.Jwts;  // Importe la classe principale pour construire et parser les tokens JWT
import io.jsonwebtoken.security.Keys;  // Importe la classe utilitaire pour générer des clés de signature sécurisées
import org.springframework.beans.factory.annotation.Value;  // Importe l'annotation pour injecter des valeurs depuis le fichier de configuration
import org.springframework.stereotype.Component;  // Importe l'annotation pour déclarer cette classe comme un bean Spring géré automatiquement
import javax.crypto.SecretKey;  // Importe l'interface représentant une clé secrète pour la signature HMAC
import java.nio.charset.StandardCharsets;  // Importe la classe définissant les jeux de caractères standards (UTF-8)
import java.util.Date;  // Importe la classe représentant une date/heure
import java.util.HashMap;  // Importe la classe HashMap pour stocker des paires clé-valeur (claims supplémentaires)
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur
import java.util.function.Function;  // Importe l'interface fonctionnelle pour appliquer des fonctions sur les claims

@Component  // Déclare cette classe comme un composant Spring (bean) automatiquement détecté et instancié
public class JwtUtil {  // Déclare la classe utilitaire pour la gestion des tokens JWT

    @Value("${jwt.secret}")  // Injecte la valeur de la propriété "jwt.secret" depuis application.properties ou application.yml
    private String secret;  // Déclare la variable contenant la clé secrète pour signer les tokens JWT

    @Value("${jwt.expiration}")  // Injecte la valeur de la propriété "jwt.expiration" depuis le fichier de configuration
    private Long expiration;  // Déclare la variable contenant la durée de validité du token en millisecondes

    private SecretKey getSigningKey() {  // Méthode privée qui génère la clé de signature à partir du secret
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);  // Convertit la chaîne secrète en tableau d'octets avec l'encodage UTF-8
        return Keys.hmacShaKeyFor(keyBytes);  // Génère et retourne une clé HMAC-SHA pour signer/vérifier les tokens
    }

    public String generateToken(String email) {  // Méthode publique qui génère un token JWT à partir d'un email (sans claims supplémentaires)
        return generateToken(new HashMap<>(), email);  // Appelle la méthode surchargée avec un HashMap vide pour les claims additionnels
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {  // Méthode publique qui génère un token avec des claims supplémentaires
        return Jwts.builder()  // Crée un nouveau constructeur de token JWT
                .claims(extraClaims)  // Ajoute les claims supplémentaires (ex: rôles, permissions) au token
                .subject(email)  // Définit le sujet du token (généralement l'email ou l'identifiant de l'utilisateur)
                .issuedAt(new Date())  // Définit la date d'émission du token (moment de création)
                .expiration(new Date(System.currentTimeMillis() + expiration))  // Définit la date d'expiration (moment actuel + durée configurée)
                .signWith(getSigningKey())  // Signe le token avec la clé secrète (garantit l'authenticité)
                .compact();  // Finalise et compacte le token en une chaîne JWT (format: header.payload.signature)
    }

    public String extractEmail(String token) {  // Méthode publique qui extrait l'email (sujet) d'un token JWT
        return extractClaim(token, Claims::getSubject);  // Extrait le sujet du token en utilisant la méthode générique d'extraction
    }

    public Date extractExpiration(String token) {  // Méthode publique qui extrait la date d'expiration d'un token JWT
        return extractClaim(token, Claims::getExpiration);  // Extrait la date d'expiration en utilisant la méthode générique d'extraction
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {  // Méthode générique qui extrait n'importe quel claim du token
        final Claims claims = extractAllClaims(token);  // Extrait tous les claims du token en une fois
        return claimsResolver.apply(claims);  // Applique la fonction fournie pour extraire le claim spécifique (ex: getSubject, getExpiration)
    }

    private Claims extractAllClaims(String token) {  // Méthode privée qui extrait tous les claims (données) d'un token JWT
        return Jwts.parser()  // Crée un parseur pour analyser les tokens JWT
                .verifyWith(getSigningKey())  // Configure le parseur avec la clé de vérification (doit correspondre à la clé de signature)
                .build()  // Construit le parseur configuré
                .parseSignedClaims(token)  // Parse le token et vérifie sa signature
                .getPayload();  // Récupère la charge utile (payload) contenant les claims
    }

    private Boolean isTokenExpired(String token) {  // Méthode privée qui vérifie si un token est expiré
        return extractExpiration(token).before(new Date());  // Compare la date d'expiration avec la date actuelle (retourne true si expiré)
    }

    public Boolean validateToken(String token, String email) {  // Méthode publique qui valide un token pour un email spécifique
        final String extractedEmail = extractEmail(token);  // Extrait l'email stocké dans le token
        return (extractedEmail.equals(email) && !isTokenExpired(token));  // Retourne true si l'email correspond ET le token n'est PAS expiré
    }

    public Boolean validateToken(String token) {  // Méthode publique surchargée qui valide un token sans comparer d'email
        try {  // Bloc try pour capturer les exceptions (token invalide, mal formé, signature incorrecte)
            extractAllClaims(token);  // Tente d'extraire et de valider les claims (vérifie signature et format)
            return !isTokenExpired(token);  // Retourne true si le token n'est PAS expiré
        } catch (Exception e) {  // Capture toute exception lors de la validation
            System.out.println(" Token invalide: " + e.getMessage());  // Affiche un log d'erreur pour le débogage
            return false;  // Retourne false si le token est invalide (expiré, mal formé, signature erronée)
        }
    }
}