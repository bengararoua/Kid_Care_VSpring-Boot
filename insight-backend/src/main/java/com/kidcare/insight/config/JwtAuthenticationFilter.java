package com.kidcare.insight.config;  // Déclare le package auquel appartient cette classe

import com.kidcare.insight.service.UserService;  // Importe le service utilisateur pour charger les données d'authentification
//Les servlets sont des composants Java côté serveur qui servent à gérer des requêtes HTTP
// Filtre permettant de chaîner les traitements HTTP
import jakarta.servlet.FilterChain;  // Importe l'interface qui permet d'enchaîner les filtres et la servlet cible
// Exception levée lors des erreurs de traitement des servlets
import jakarta.servlet.ServletException;  // Importe l'exception spécifique aux erreurs de traitement des servlets
import jakarta.servlet.http.HttpServletRequest;  // Importe l'objet représentant la requête HTTP entrante
import jakarta.servlet.http.HttpServletResponse;  // Importe l'objet représentant la réponse HTTP sortante
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // Importe le token d'authentification contenant les identifiants utilisateur
import org.springframework.security.core.context.SecurityContextHolder;  // Importe le contexte de sécurité où est stocké l'utilisateur authentifié
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur authentifié
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;  // Importe la classe qui ajoute les détails de la requête (IP, session) à l'authentification
import org.springframework.web.filter.OncePerRequestFilter;  // Importe la classe mère garantissant que le filtre s'exécute une seule fois par requête
import java.io.IOException;  // Importe l'exception pour les erreurs d'entrée/sortie
import java.util.Arrays;  // Importe la classe utilitaire pour créer et manipuler des tableaux
import java.util.List;  // Importe l'interface List pour stocker une collection ordonnée d'éléments

public class JwtAuthenticationFilter extends OncePerRequestFilter {  // Déclare une classe de filtre JWT qui hérite de OncePerRequestFilter

    private final JwtUtil jwtUtil;  // Déclare une variable finale pour l'utilitaire de gestion des tokens JWT
    private final UserService userService;  // Déclare une variable finale pour le service utilisateur

    // Liste des endpoints publics (sans authentification)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(  // Déclare une liste constante contenant les chemins d'API accessibles sans token
        "/api/register",  // Endpoint d'inscription - public
        "/api/login",  // Endpoint de connexion - public
        "/api/password/email",  // Endpoint pour demander la réinitialisation du mot de passe - public
        "/api/password/reset",  // Endpoint pour réinitialiser le mot de passe avec le token reçu par email - public
        "/api/password/check-token",  // Endpoint pour vérifier la validité d'un token de réinitialisation - public
        "/api/test/",  // Endpoint de test pour vérifier que l'API fonctionne - public
        "/api/health"  // Endpoint de santé pour le monitoring des serveurs - public
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {  // Constructeur du filtre prenant les dépendances nécessaires
        this.jwtUtil = jwtUtil;  // Assigne l'utilitaire JWT reçu à la variable d'instance
        this.userService = userService;  // Assigne le service utilisateur reçu à la variable d'instance
    }

    @Override  // Indique que cette méthode surcharge une méthode de la classe parente
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {  // Détermine si le filtre doit ignorer cette requête
        String path = request.getRequestURI();  // Récupère le chemin de l'URL demandée (ex: /api/login)
        //  AJOUTE LES REQUÊTES OPTIONS
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {  // Vérifie si la méthode HTTP est OPTIONS (pré-requête CORS)
            return true;  // Retourne true pour que le filtre ignore cette requête OPTIONS
        }
        //stream():Transformer une collection en séquence d'éléments que l'on peut traiter
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);  // Vérifie si le chemin commence par un endpoint public et retourne true pour ignorer le filtre
    }

    @Override  // Indique que cette méthode surcharge une méthode de la classe parente
    protected void doFilterInternal(HttpServletRequest request,  // Requête HTTP entrante
                                    HttpServletResponse response,  // Réponse HTTP sortante
                                    FilterChain chain)  // Chaîne de filtres à exécuter ensuite
            throws ServletException, IOException {  // Déclare les exceptions pouvant être lancées

        //  AJOUTE LES HEADERS CORS POUR LES RÉPONSES
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");  // Autorise uniquement le frontend React à communiquer avec cette API
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");  // Liste les méthodes HTTP autorisées pour les requêtes cross-origin
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");  // Liste les en-têtes HTTP autorisés dans les requêtes cross-origin
        response.setHeader("Access-Control-Allow-Credentials", "true");  // Autorise l'envoi de cookies et d'en-têtes d'authentification cross-origin

        //  GÈRE LES REQUÊTES OPTIONS DIRECTEMENT
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {  // Vérifie à nouveau si c'est une requête OPTIONS (pré-requête CORS)
            response.setStatus(HttpServletResponse.SC_OK);  // Répond avec le code HTTP 200 (OK) sans traitement supplémentaire
            return;  // Arrête l'exécution du filtre ici pour les requêtes OPTIONS
        }

        final String authHeader = request.getHeader("Authorization");  // Récupère l'en-tête Authorization contenant le token JWT

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {  // Vérifie si l'en-tête est absent ou ne commence pas par "Bearer "
            chain.doFilter(request, response);  // Passe la requête au filtre suivant sans authentification
            return;  // Sort de la méthode car pas de token valide à traiter
        }

        final String token = authHeader.substring(7);  // Extrait le token JWT en supprimant le préfixe "Bearer " (7 caractères)
        
        try {  // Bloc try-catch pour capturer les erreurs potentielles lors de l'authentification
            final String email = jwtUtil.extractEmail(token);  // Extrait l'email de l'utilisateur depuis le token JWT
            System.out.println("📧 Email extrait du token: " + email);  // Affiche un log de l'email extrait (pour débogage)

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {  // Vérifie si l'email existe et si aucun utilisateur n'est déjà authentifié
                UserDetails userDetails = userService.loadUserByUsername(email);  // Charge les détails de l'utilisateur depuis la base de données via son email

                if (jwtUtil.validateToken(token, email)) {  // Valide que le token JWT est toujours valide (non expiré, signature correcte)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(  // Crée un token d'authentification Spring Security
                        userDetails,  // Les détails de l'utilisateur (email, mot de passe, rôles)
                        null,  // Les identifiants (null car on utilise déjà le token JWT)
                        userDetails.getAuthorities()  // Les rôles et permissions de l'utilisateur
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));  // Ajoute les détails de la requête (IP, session) au token
                    SecurityContextHolder.getContext().setAuthentication(authToken);  // Enregistre l'utilisateur authentifié dans le contexte de sécurité Spring
                    System.out.println(" Authentification réussie pour: " + email);  // Log de succès pour le débogage
                } else {  // Cas où le token est invalide ou expiré
                    System.out.println(" Token invalide ou expiré pour: " + email);  // Log d'erreur pour le débogage
                }
            }
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            System.out.println(" Erreur lors de l'authentification: " + e.getMessage());  // Log l'erreur pour le débogage
        }

        chain.doFilter(request, response);  // Passe la requête au filtre suivant ou à la servlet cible
    }
}