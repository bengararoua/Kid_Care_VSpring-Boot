package com.kidcare.insight.config;  // Déclare le package auquel appartient cette classe de configuration sécurité

import com.kidcare.insight.service.UserService;  // Importe le service utilisateur pour charger les données d'authentification
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.context.annotation.Bean;  // Importe l'annotation pour déclarer un bean Spring géré par le conteneur
import org.springframework.context.annotation.Configuration;  // Importe l'annotation pour indiquer que cette classe contient des configurations Spring
import org.springframework.context.annotation.Lazy;  // Importe l'annotation pour retarder l'injection d'une dépendance (évite les cycles)
import org.springframework.http.HttpMethod;  // Importe l'enumération des méthodes HTTP (GET, POST, PUT, DELETE, etc.)
import org.springframework.security.authentication.AuthenticationManager;  // Importe l'interface qui gère l'authentification des utilisateurs
import org.springframework.security.authentication.AuthenticationProvider;  // Importe l'interface qui fournit la logique d'authentification
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;  // Importe le fournisseur d'authentification qui utilise UserDetailsService
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;  // Importe la classe pour accéder à la configuration d'authentification
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // Importe le builder pour configurer la sécurité HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // Importe l'annotation pour activer la sécurité web Spring
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;  // Importe la classe pour désactiver des configurateurs spécifiques
import org.springframework.security.config.http.SessionCreationPolicy;  // Importe l'énumération pour définir la politique de création de sessions
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // Importe l'encodeur de mot de passe BCrypt (hachage sécurisé)
import org.springframework.security.crypto.password.PasswordEncoder;  // Importe l'interface pour l'encodage des mots de passe
import org.springframework.security.web.SecurityFilterChain;  // Importe l'interface représentant la chaîne de filtres de sécurité
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  // Importe le filtre standard d'authentification par login/password
import org.springframework.web.cors.CorsConfiguration;  // Importe la classe contenant la configuration CORS (origines, méthodes, en-têtes autorisés)
import org.springframework.web.cors.CorsConfigurationSource;  // Importe l'interface source de configuration CORS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;  // Importe l'implémentation qui associe des configs CORS à des chemins URL

import java.util.Arrays;  // Importe la classe utilitaire pour créer et manipuler des tableaux
import java.util.List;  // Importe l'interface List pour stocker des collections ordonnées

@Configuration  // Déclare cette classe comme une source de définitions de beans Spring
@EnableWebSecurity  // Active la sécurité web Spring Security pour l'application
public class SecurityConfig {  // Déclare la classe de configuration principale de la sécurité

    private final JwtUtil jwtUtil;  // Déclare une variable finale pour l'utilitaire de gestion des tokens JWT
    private final UserService userService;  // Déclare une variable finale pour le service utilisateur

    @Lazy  // Retarde l'injection du bean pour éviter un cycle de dépendances
    @Autowired  // Injecte automatiquement les beans JwtUtil et UserService via le constructeur
    public SecurityConfig(JwtUtil jwtUtil, UserService userService) {  // Constructeur de la configuration avec injection des dépendances
        this.jwtUtil = jwtUtil;  // Assigne l'utilitaire JWT reçu à la variable d'instance
        this.userService = userService;  // Assigne le service utilisateur reçu à la variable d'instance
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring à gérer par le conteneur
    public JwtAuthenticationFilter jwtAuthenticationFilter() {  // Crée et configure le filtre d'authentification JWT
        return new JwtAuthenticationFilter(jwtUtil, userService);  // Instancie le filtre avec les dépendances nécessaires (JWT + UserService)
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring (fournisseur d'authentification)
    public AuthenticationProvider authenticationProvider() {  // Configure le fournisseur d'authentification pour Spring Security
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();  // Crée un fournisseur DAO (Database Authentication Provider)
        authProvider.setUserDetailsService(userService);  // Définit le service qui charge les utilisateurs depuis la base de données
        authProvider.setPasswordEncoder(passwordEncoder());  // Définit l'encodeur de mots de passe (BCrypt pour comparer les hashs)
        return authProvider;  // Retourne le fournisseur configuré
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring (encodeur de mots de passe)
    public PasswordEncoder passwordEncoder() {  // Configure l'encodeur de mots de passe utilisé pour hasher et vérifier les mots de passe
        return new BCryptPasswordEncoder();  // Retourne un encodeur BCrypt (algorithme de hachage sécurisé avec salage automatique)
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring (gestionnaire d'authentification)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {  // Récupère le gestionnaire d'authentification depuis la configuration
        return authConfig.getAuthenticationManager();  // Retourne le AuthenticationManager configuré par Spring
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring (source de configuration CORS)
    public CorsConfigurationSource corsConfigurationSource() {  // Configure les règles CORS pour autoriser les requêtes cross-origin
        CorsConfiguration configuration = new CorsConfiguration();  // Crée un objet de configuration CORS
        
        // Configuration des origines autorisées (frontends qui peuvent appeler l'API)
        configuration.setAllowedOrigins(List.of(  // Définit la liste des domaines autorisés
                "http://localhost:3000",  // Origine du frontend React (port standard)
                "http://localhost:8081",  // Autre origine possible (autre frontend ou dev)
                "http://127.0.0.1:3000",  // Même origine mais avec l'adresse IP locale
                "http://localhost:5173"   // Origine pour Vite (autre bundler frontend)
        ));
        
        // Configuration des méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(  // Définit les verbes HTTP acceptés
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"  // Méthodes standards pour une API REST
        ));
        
        // Configuration des en-têtes HTTP autorisés dans les requêtes
        configuration.setAllowedHeaders(Arrays.asList(  // Définit les headers que le frontend peut envoyer
                "Authorization",  // En-tête pour le token JWT (Bearer token)
                "Content-Type",   // En-tête pour le type de contenu (JSON, formulaire, etc.)
                "Accept",         // En-tête pour le format de réponse souhaité
                "X-Requested-With",  // En-tête pour identifier les requêtes AJAX
                "Cache-Control"   // En-tête pour contrôler la mise en cache
        ));
        
        configuration.setExposedHeaders(List.of("Authorization"));  // Rend l'en-tête Authorization accessible au frontend JavaScript
        configuration.setAllowCredentials(true);  // Autorise l'envoi de cookies et d'identifiants cross-origin
        configuration.setMaxAge(3600L);  // Durée de cache de la configuration CORS (3600 secondes = 1 heure)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  // Crée une source de config CORS basée sur les URLs
        source.registerCorsConfiguration("/**", configuration);  // Applique cette configuration CORS à tous les chemins de l'API
        return source;  // Retourne la source de configuration CORS
    }

    @Bean  // Déclare que cette méthode retourne un bean Spring (chaîne de filtres de sécurité)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // Configure la chaîne complète des filtres de sécurité Spring
        http  // Commence la configuration de la sécurité HTTP
                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Active CORS avec la configuration personnalisée définie plus haut
                
                // Désactivation des protections (API stateless = pas besoin de CSRF)
                .csrf(AbstractHttpConfigurer::disable)  // Désactive la protection CSRF (car API stateless sans session, et gère déjà le CORS)
                .httpBasic(AbstractHttpConfigurer::disable)  // Désactive l'authentification HTTP Basic (car on utilise JWT)
                .formLogin(AbstractHttpConfigurer::disable)  // Désactive le formulaire de login par défaut de Spring Security
                
                // Configuration de la gestion des sessions
                .sessionManagement(session -> session  // Configure la politique de gestion des sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Rend l'API sans état (stateless) - pas de session HTTP créée
                
                // Configuration des fournisseurs d'authentification
                .authenticationProvider(authenticationProvider())  // Enregistre le fournisseur d'authentification DAO (utilise UserService + BCrypt)
                
                // Configuration des filtres personnalisés
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)  // Ajoute le filtre JWT AVANT le filtre standard (intercepte avant)
                
                // Configuration des règles d'autorisation des requêtes
                .authorizeHttpRequests(auth -> auth  // Commence la configuration des règles d'accès par endpoint
                        
                        // ===== ENDPOINTS PUBLICS (sans authentification) =====
                        .requestMatchers(  // Définit les endpoints accessibles sans token
                                "/api/register",  // Endpoint d'inscription - public
                                "/api/login",     // Endpoint de connexion - public
                                "/api/password/**",  // Tous les endpoints de gestion du mot de passe - public
                                "/api/test/**",   // Endpoints de test - public
                                "/api/health",    // Endpoint de santé pour monitoring - public
                                "/api/behavioral-test/questions",  // Récupération des questions du test comportemental - public
                                "/api/behavioral-test/*/history",  // Historique des tests comportementaux - public
                                //  AJOUT POUR GRAPHQL
                                "/graphiql",      // Interface GraphiQL (console interactive GraphQL) - public
                                "/graphql"        // Endpoint GraphQL principal - public
                        ).permitAll()  // Permet l'accès à tous ces endpoints sans authentification

                        // ===== REQUÊTES OPTIONS (pré-requêtes CORS) =====
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Toutes les requêtes OPTIONS sont publiques (nécessaire pour CORS)

                        // ===== ACTION PLAN (plan d'action) =====
                        .requestMatchers("/api/action-plan/**").authenticated()  // Routes du plan d'action - nécessite authentification

                        // ===== ROUTINES (routines quotidiennes) =====
                        .requestMatchers("/api/routines/**").authenticated()  // Routes des routines - nécessite authentification

                        // ===== BEHAVIORAL TEST (test comportemental) =====
                        .requestMatchers("/api/behavioral-test/*/submit").authenticated()  // Soumission des réponses au test - nécessite authentification

                        // ===== CHILDREN (gestion des enfants) =====
                        .requestMatchers("/api/children/**").authenticated()  // Routes des enfants (CRUD) - nécessite authentification

                        // ===== LOGS (journaux d'activité) =====
                        .requestMatchers("/api/logs/**").authenticated()  // Routes des logs système - nécessite authentification

                        // ===== EXPORT (export de données) =====
                        .requestMatchers("/api/export/**").authenticated()  // Routes d'export de données - nécessite authentification

                        // ===== RECOMMENDATIONS (recommandations personnalisées) =====
                        .requestMatchers("/api/children/*/recommendations").authenticated()  // Recommandations pour un enfant spécifique - nécessite authentification
                        .requestMatchers("/api/recommendations/*/toggle").authenticated()  // Activation/désactivation d'une recommandation - nécessite authentification
                        .requestMatchers("/api/recommendations/*").authenticated()  // Routes des recommandations (CRUD) - nécessite authentification

                        // ===== NOTES (notes personnelles) =====
                        .requestMatchers("/api/children/*/notes").authenticated()  // Notes pour un enfant spécifique - nécessite authentification
                        .requestMatchers("/api/notes/*").authenticated()  // Routes des notes (CRUD) - nécessite authentification

                        // ===== USERS (gestion des utilisateurs) =====
                        .requestMatchers("/api/users/**").authenticated()  // Routes utilisateur (admin/profil) - nécessite authentification

                        // ===== MESSAGES (messagerie) =====
                        .requestMatchers("/api/messages/**").authenticated()  // Routes des messages - nécessite authentification

                        // ===== ME (profil personnel) =====
                        .requestMatchers("/api/me").authenticated()  // Route pour récupérer ses propres informations - nécessite authentification

                        // ===== DASHBOARD (tableau de bord) =====
                        .requestMatchers("/api/dashboard/**").authenticated()  // Routes du tableau de bord - nécessite authentification

                        // ===== APPOINTMENTS (rendez-vous) =====
                        .requestMatchers("/api/appointments/**").authenticated()  // Routes des rendez-vous - nécessite authentification

                        // ===== NOTIFICATIONS (notifications) =====
                        .requestMatchers("/api/notifications/**").authenticated()  // Routes des notifications - nécessite authentification

                        // ===== PROFIL (profil utilisateur - toutes méthodes) =====
                        .requestMatchers(HttpMethod.GET, "/api/profile/**").authenticated()  // Lecture du profil - nécessite authentification
                        .requestMatchers(HttpMethod.PUT, "/api/profile/**").authenticated()  // Mise à jour du profil - nécessite authentification
                        .requestMatchers(HttpMethod.POST, "/api/profile/**").authenticated()  // Création de profil - nécessite authentification
                        .requestMatchers(HttpMethod.DELETE, "/api/profile/**").authenticated()  // Suppression de profil - nécessite authentification

                        // ===== TOUTE AUTRE REQUÊTE (non spécifiée ci-dessus) =====
                        .anyRequest().authenticated()  // Tous les endpoints non listés nécessitent une authentification valide
                );

        return http.build();  // Construit et retourne la configuration complète de la chaîne de filtres de sécurité
    }
}