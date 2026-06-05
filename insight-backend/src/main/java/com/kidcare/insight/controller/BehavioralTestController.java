package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.fasterxml.jackson.core.type.TypeReference;  // Importe l'utilitaire pour les types génériques Jackson (permet de désérialiser List<Map>)
import com.fasterxml.jackson.databind.ObjectMapper;  // Importe le mapper JSON pour convertir objet <-> JSON
import com.kidcare.insight.entity.BehavioralTest;  // Importe l'entité représentant un test comportemental passé par un enfant
import com.kidcare.insight.entity.Child;  // Importe l'entité représentant un enfant suivi par un parent/enseignant
import com.kidcare.insight.entity.Notification;  // Importe l'entité représentant une notification envoyée aux utilisateurs
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.BehavioralTestRepository;  // Importe le repository JPA pour les tests comportementaux
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository JPA pour les enfants
import com.kidcare.insight.repository.NotificationRepository;  // Importe le repository JPA pour les notifications
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour les utilisateurs
import org.slf4j.Logger;  // Importe l'interface de logging SLF4J pour tracer l'exécution
import org.slf4j.LoggerFactory;  // Importe la fabrique pour créer des loggers
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe pour construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, etc.)

import java.time.LocalDateTime;  // Importe la classe pour manipuler les dates et heures
import java.util.*;  // Importe toutes les collections Java (List, Map, ArrayList, HashMap, LinkedHashMap, etc.)

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/behavioral-test")  // Définit le préfixe commun "/api/behavioral-test" pour tous les endpoints de ce contrôleur
public class BehavioralTestController {  // Déclare le contrôleur gérant les tests comportementaux (dépistage TSA, TDAH, etc.)

    private static final Logger log = LoggerFactory.getLogger(BehavioralTestController.class);  // Crée un logger pour tracer l'exécution et déboguer

    @Autowired  // Injecte automatiquement le bean ChildRepository géré par Spring
    private ChildRepository childRepository;  // Repository pour accéder aux données des enfants en base de données

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @Autowired  // Injecte automatiquement le bean BehavioralTestRepository géré par Spring
    private BehavioralTestRepository behavioralTestRepository;  // Repository pour accéder aux tests comportementaux en base de données

    @Autowired  // Injecte automatiquement le bean NotificationRepository géré par Spring
    private NotificationRepository notificationRepository;  // Repository pour accéder aux notifications en base de données

    private final ObjectMapper objectMapper = new ObjectMapper();  // Crée un mapper JSON pour convertir les objets Java en JSON et vice-versa

    // ===== MÉTHODE DE CONSTRUCTION DES QUESTIONS =====
    private Map<String, List<Map<String, Object>>> buildQuestions() {  // Méthode privée qui construit la banque complète des questions du test
        Map<String, List<Map<String, Object>>> questions = new LinkedHashMap<>();  // Crée une map qui préserve l'ordre d'insertion (LinkedHashMap)

        // CATÉGORIE 1: COMMUNICATION (5 questions)
        List<Map<String, Object>> communication = new ArrayList<>();  // Crée une liste pour stocker les 5 questions de communication
        
        Map<String, String> optionsStandard = Map.of(  // Crée une map des options de réponse standard (commune à plusieurs questions)
                "always", "Toujours",  // Option "always" affichée "Toujours"
                "usually", "Souvent",  // Option "usually" affichée "Souvent"
                "sometimes", "Parfois",  // Option "sometimes" affichée "Parfois"
                "rarely", "Rarement",  // Option "rarely" affichée "Rarement"
                "never", "Jamais"  // Option "never" affichée "Jamais"
        );
        
        Map<String, Integer> weightsStandard = Map.of(  // Crée une map des poids (scores) pour chaque option
                "always", 1,  // "Toujours" = score 1 (comportement souhaitable)
                "usually", 2,  // "Souvent" = score 2
                "sometimes", 3,  // "Parfois" = score 3
                "rarely", 4,  // "Rarement" = score 4
                "never", 5  // "Jamais" = score 5 (comportement problématique)
        );
        
        Map<String, String> optionsInverse = Map.of(  // Crée une map des options inversées (pour les questions où "jamais" est bon)
                "never", "Jamais",  // Option "never" affichée "Jamais"
                "rarely", "Rarement",  // Option "rarely" affichée "Rarement"
                "sometimes", "Parfois",  // Option "sometimes" affichée "Parfois"
                "usually", "Souvent",  // Option "usually" affichée "Souvent"
                "always", "Toujours"  // Option "always" affichée "Toujours"
        );
        
        Map<String, Integer> weightsInverse = Map.of(  // Crée une map des poids inversés
                "never", 1,  // "Jamais" = score 1 (comportement souhaitable)
                "rarely", 2,  // "Rarement" = score 2
                "sometimes", 3,  // "Parfois" = score 3
                "usually", 4,  // "Souvent" = score 4
                "always", 5  // "Toujours" = score 5 (comportement problématique)
        );
        
        communication.add(createQuestion("comm_1", "L'enfant réagit-il quand on l'appelle par son prénom ?", optionsStandard, weightsStandard));  // Ajoute question communication 1
        communication.add(createQuestion("comm_2", "L'enfant utilise-t-il des mots ou des gestes pour demander ce qu'il veut ?", optionsStandard, weightsStandard));  // Ajoute question communication 2
        communication.add(createQuestion("comm_3", "L'enfant comprend-il les consignes simples ?", optionsStandard, weightsStandard));  // Ajoute question communication 3
        communication.add(createQuestion("comm_4", "L'enfant initie-t-il des interactions avec d'autres enfants ?", optionsStandard, weightsStandard));  // Ajoute question communication 4
        communication.add(createQuestion("comm_5", "L'enfant répond-il aux questions par des phrases complètes (selon son âge) ?", optionsStandard, weightsStandard));  // Ajoute question communication 5

        // CATÉGORIE 2: SOCIAL (5 questions)
        List<Map<String, Object>> social = new ArrayList<>();  // Crée une liste pour stocker les 5 questions sociales
        
        Map<String, String> optionsAmis = Map.of(  // Crée une map d'options spécifique pour la question sur les amis
                "yes_many", "Oui, plusieurs",  // Option "plusieurs amis"
                "yes_few", "Oui, quelques-uns",  // Option "quelques amis"
                "hardly", "Difficilement",  // Option "difficilement"
                "none", "Aucun"  // Option "aucun ami"
        );
        
        Map<String, Integer> weightsAmis = Map.of(  // Crée une map des poids pour la question sur les amis
                "yes_many", 1,  // "Plusieurs amis" = score 1
                "yes_few", 2,  // "Quelques amis" = score 2
                "hardly", 4,  // "Difficilement" = score 4
                "none", 5  // "Aucun" = score 5
        );
        
        social.add(createQuestion("soc_1", "L'enfant a-t-il des amis de son âge ?", optionsAmis, weightsAmis));  // Ajoute question social 1 (spécifique amis)
        social.add(createQuestion("soc_2", "L'enfant cherche-t-il à jouer avec d'autres enfants ?", optionsStandard, weightsStandard));  // Ajoute question social 2
        social.add(createQuestion("soc_3", "L'enfant comprend-il les règles sociales de base (tour de rôle, partage) ?", optionsStandard, weightsStandard));  // Ajoute question social 3
        social.add(createQuestion("soc_4", "L'enfant montre-t-il de l'empathie envers les autres ?", optionsStandard, weightsStandard));  // Ajoute question social 4
        social.add(createQuestion("soc_5", "L'enfant participe-t-il à des activités de groupe ?", optionsStandard, weightsStandard));  // Ajoute question social 5

        // CATÉGORIE 3: ATTENTION (5 questions)
        List<Map<String, Object>> attention = new ArrayList<>();  // Crée une liste pour stocker les 5 questions d'attention
        
        attention.add(createQuestion("att_1", "L'enfant arrive-t-il à rester concentré sur une activité pendant 10-15 minutes ?", optionsStandard, weightsStandard));  // Ajoute question attention 1
        attention.add(createQuestion("att_2", "L'enfant est-il facilement distrait par ce qui l'entoure ?", optionsInverse, weightsInverse));  // Ajoute question attention 2 (inversée)
        attention.add(createQuestion("att_3", "L'enfant termine-t-il ses tâches avant de passer à autre chose ?", optionsStandard, weightsStandard));  // Ajoute question attention 3
        attention.add(createQuestion("att_4", "L'enfant a-t-il du mal à suivre les instructions en plusieurs étapes ?", optionsInverse, weightsInverse));  // Ajoute question attention 4 (inversée)
        attention.add(createQuestion("att_5", "L'enfant semble-t-il agité ou a-t-il du mal à rester assis ?", optionsInverse, weightsInverse));  // Ajoute question attention 5 (inversée)

        // CATÉGORIE 4: COMPORTEMENT (5 questions)
        List<Map<String, Object>> behavior = new ArrayList<>();  // Crée une liste pour stocker les 5 questions de comportement
        
        behavior.add(createQuestion("beh_1", "L'enfant a-t-il des crises de colère ou des comportements explosifs ?", optionsInverse, weightsInverse));  // Ajoute question comportement 1 (inversée)
        behavior.add(createQuestion("beh_2", "L'enfant respecte-t-il les règles et les limites ?", optionsStandard, weightsStandard));  // Ajoute question comportement 2
        behavior.add(createQuestion("beh_3", "L'enfant a-t-il des comportements répétitifs (se balancer, tourner, aligner des objets) ?", optionsInverse, weightsInverse));  // Ajoute question comportement 3 (inversée)
        behavior.add(createQuestion("beh_4", "L'enfant s'adapte-t-il facilement aux changements de routine ?", optionsStandard, weightsStandard));  // Ajoute question comportement 4
        behavior.add(createQuestion("beh_5", "L'enfant a-t-il des intérêts très spécifiques ou intenses ?", optionsInverse, weightsInverse));  // Ajoute question comportement 5 (inversée)

        questions.put("communication", communication);  // Ajoute la catégorie "communication" avec ses 5 questions dans la map principale
        questions.put("social", social);  // Ajoute la catégorie "social" avec ses 5 questions dans la map principale
        questions.put("attention", attention);  // Ajoute la catégorie "attention" avec ses 5 questions dans la map principale
        questions.put("behavior", behavior);  // Ajoute la catégorie "behavior" (comportement) avec ses 5 questions dans la map principale

        return questions;  // Retourne la map complète contenant 20 questions réparties en 4 catégories
    }

    private Map<String, Object> createQuestion(String id, String text, Map<String, String> options, Map<String, Integer> weights) {  // Méthode utilitaire pour créer une question individuelle
        Map<String, Object> question = new LinkedHashMap<>();  // Crée une map qui préserve l'ordre d'insertion pour la question
        question.put("id", id);  // Ajoute l'identifiant unique de la question (ex: "comm_1", "soc_2")
        question.put("text", text);  // Ajoute le texte de la question en français
        question.put("options", options);  // Ajoute la map des options de réponse (clé technique -> texte affiché)
        question.put("weights", weights);  // Ajoute la map des poids (scores) pour chaque option
        return question;  // Retourne la question complète prête à être utilisée
    }

    // ===== ENDPOINT 1: RÉCUPÉRER LES QUESTIONS =====
    @GetMapping("/questions")  // Associe les requêtes GET à "/api/behavioral-test/questions" à cette méthode
    public ResponseEntity<Map<String, Object>> getQuestions() {  // Récupère toutes les questions du test (endpoint public)
        log.info("GET /api/behavioral-test/questions - Récupération des questions");  // Log de l'appel pour débogage
        Map<String, Object> response = new HashMap<>();  // Crée une map pour construire la réponse JSON
        
        Map<String, List<Map<String, Object>>> questions = buildQuestions();  // Appelle la méthode pour construire les 20 questions
        response.put("questions", questions);  // Ajoute les questions structurées par catégorie dans la réponse

        Map<String, Map<String, String>> categories = new HashMap<>();  // Crée une map pour stocker les métadonnées des catégories
        categories.put("communication", Map.of("name", "Communication", "icon", "💬", "color", "#3b82f6"));  // Métadonnées de la catégorie communication (nom, icône, couleur bleue)
        categories.put("social", Map.of("name", "Social", "icon", "👥", "color", "#10b981"));  // Métadonnées de la catégorie social (nom, icône, couleur verte)
        categories.put("attention", Map.of("name", "Attention", "icon", "🧠", "color", "#f59e0b"));  // Métadonnées de la catégorie attention (nom, icône, couleur orange)
        categories.put("behavior", Map.of("name", "Comportement", "icon", "🎯", "color", "#ef4444"));  // Métadonnées de la catégorie comportement (nom, icône, couleur rouge)
        response.put("categories", categories);  // Ajoute les métadonnées des catégories dans la réponse

        return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec les questions et les métadonnées en JSON
    }

    // ===== ENDPOINT 2: SOUMETTRE UN TEST =====
    @PostMapping("/{childId}/submit")  // Associe les requêtes POST à "/api/behavioral-test/{childId}/submit" à cette méthode
    public ResponseEntity<Map<String, Object>> submitTest(  // Soumet les réponses d'un test pour un enfant spécifique
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /5/submit)
            @RequestBody Map<String, Object> requestBody,  // Extrait le corps JSON de la requête contenant les réponses
            @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié depuis le token JWT

        log.info("===== DEBUT SOUMISSION TEST =====");  // Log de début de traitement avec séparateur visible
        log.info("ChildId: {}", childId);  // Log de l'ID de l'enfant pour lequel le test est passé
        log.info("Request body: {}", requestBody);  // Log du corps complet de la requête pour débogage

        try {  // Bloc try-catch pour capturer et gérer toutes les erreurs potentielles
            User user = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base de données par son email
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance une exception si l'utilisateur n'existe pas
            log.info("User: {} - Role: {}", user.getEmail(), user.getRole());  // Log de l'email et du rôle de l'utilisateur connecté

            Child child = childRepository.findById(childId)  // Cherche l'enfant en base de données par son ID
                    .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));  // Lance une exception si l'enfant n'existe pas
            log.info("Child: {}", child.getName());  // Log du nom de l'enfant pour lequel le test est passé

            // VÉRIFICATION DES PERMISSIONS (SÉCURITÉ)
            if (user.isParent() && (child.getParent() == null || !child.getParent().getId().equals(user.getId()))) {  // Vérifie si l'utilisateur est parent ET si l'enfant lui appartient
                log.warn("Parent non autorisé: user={}, child parent={}", user.getId(), child.getParent() != null ? child.getParent().getId() : "null");  // Log d'avertissement avec détails
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));  // Retourne HTTP 403 Forbidden avec message d'erreur
            }
            
            if (user.isTeacher() && (child.getTeacher() == null || !child.getTeacher().getId().equals(user.getId()))) {  // Vérifie si l'utilisateur est enseignant ET si l'enfant lui est assigné
                log.warn("Teacher non autorisé");  // Log d'avertissement
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));  // Retourne HTTP 403 Forbidden
            }

            // EXTRACTION DES RÉPONSES DU CORPS DE LA REQUÊTE
            Map<String, String> responses = new HashMap<>();  // Crée une map pour stocker les réponses (clé: id question, valeur: réponse choisie)

            if (requestBody.containsKey("responses")) {  // Vérifie si le champ "responses" existe dans le corps JSON
                Object responsesObj = requestBody.get("responses");  // Récupère l'objet "responses" (peut être Map ou autre type)
                if (responsesObj instanceof Map) {  // Vérifie que l'objet est bien une Map (structure clé-valeur)
                    Map<?, ?> tempMap = (Map<?, ?>) responsesObj;  // Cast l'objet en Map générique
                    for (Map.Entry<?, ?> entry : tempMap.entrySet()) {  // Parcourt chaque entrée de la map
                        responses.put(entry.getKey().toString(), entry.getValue().toString());  // Ajoute la réponse à la map (convertit clé et valeur en String)
                    }
                }
            }

            if (responses.isEmpty()) {  // Vérifie si aucune réponse valide n'a été trouvée
                log.error("Aucune réponse valide trouvée");  // Log d'erreur
                return ResponseEntity.badRequest().body(Map.of("error", "Aucune réponse fournie"));  // Retourne HTTP 400 Bad Request
            }

            log.info("Nombre de réponses: {}", responses.size());  // Log du nombre de réponses reçues (devrait être 20)

            // CALCUL DES SCORES ET GÉNÉRATION DES RÉSULTATS
            Map<String, Object> scores = calculateScores(responses);  // Calcule les scores par catégorie et le score global à partir des réponses
            String riskLevel = determineRiskLevel((Map<String, Object>) scores.get("overall"));  // Détermine le niveau de risque (low/moderate/high) basé sur le score global
            Map<String, Object> feedback = generateFeedback(scores, riskLevel);  // Génère les feedbacks personnalisés (message global + messages détaillés par catégorie)
            List<String> recommendations = generateRecommendations(scores, riskLevel);  // Génère la liste des recommandations personnalisées

            // SAUVEGARDE DU TEST EN BASE DE DONNÉES
            BehavioralTest test = new BehavioralTest();  // Crée une nouvelle instance de l'entité BehavioralTest
            test.setChild(child);  // Associe l'enfant au test (relation ManyToOne)
            test.setUser(user);  // Associe l'utilisateur (parent/teacher) au test
            test.setUserRole(user.isParent() ? "parent" : "teacher");  // Définit le rôle de l'utilisateur qui a passé le test (pour historique)
            test.setResponses(objectMapper.writeValueAsString(responses));  // Convertit la map des réponses en JSON et la sauvegarde
            
            // Sérialisation et sauvegarde des scores
            String scoresJson = objectMapper.writeValueAsString(scores);  // Convertit la map des scores en JSON
            test.setScores(scoresJson);  // Sauvegarde les scores en JSON
            
            test.setRiskLevel(riskLevel);  // Sauvegarde le niveau de risque (low/moderate/high)
            test.setFeedbackMessage((String) feedback.get("message"));  // Sauvegarde le message de feedback principal
            
            // Sérialisation et sauvegarde des recommandations
            String recommendationsJson = objectMapper.writeValueAsString(recommendations);  // Convertit la liste des recommandations en JSON
            test.setRecommendations(recommendationsJson);  // Sauvegarde les recommandations en JSON

            behavioralTestRepository.save(test);  // Persiste (enregistre) le test dans la base de données
            log.info("Test sauvegardé avec ID: {}", test.getId());  // Log de confirmation avec l'ID généré automatiquement

            // CRÉATION D'UNE NOTIFICATION POUR LE PARENT (si c'est un enseignant qui a passé le test)
            if (user.isTeacher() && child.getParent() != null) {  // Vérifie si l'utilisateur est enseignant ET si l'enfant a un parent associé
                Notification notification = new Notification();  // Crée une nouvelle instance de l'entité Notification
                notification.setUser(child.getParent());  // Associe la notification au parent de l'enfant (destinataire)
                notification.setType("test_result");  // Définit le type de notification comme "résultat de test"
                notification.setTitle("📋 Résultat du test comportemental");  // Définit le titre de la notification
                notification.setMessage("Un test a été réalisé pour " + child.getName() + ". Niveau: " + getRiskLabel(riskLevel));  // Définit le message avec le nom enfant et niveau risque
                notification.setData("{\"testId\":" + test.getId() + ",\"childId\":" + childId + "}");  // Ajoute des données JSON supplémentaires (ID test, ID enfant)
                notification.setIsRead(false);  // Marque la notification comme non lue
                notificationRepository.save(notification);  // Persiste (enregistre) la notification dans la base de données
                log.info("Notification envoyée au parent: {}", child.getParent().getEmail());  // Log de confirmation avec l'email du parent
            }

            // CONSTRUCTION DE LA RÉPONSE JSON À RENVOYER AU FRONTEND
            Map<String, Object> result = new HashMap<>();  // Crée une map pour la réponse JSON
            result.put("success", true);  // Ajoute un indicateur de succès
            
            Map<String, Object> testInfo = new HashMap<>();  // Crée une map pour les informations du test
            testInfo.put("id", test.getId());  // Ajoute l'ID du test généré
            testInfo.put("created_at", test.getCreatedAt());  // Ajoute la date et heure de création du test
            result.put("test", testInfo);  // Ajoute les infos du test à la réponse
            
            result.put("scores", scores);  // Ajoute les scores détaillés (par catégorie et global)
            result.put("risk_level", riskLevel);  // Ajoute le niveau de risque (low/moderate/high)
            result.put("risk_label", getRiskLabel(riskLevel));  // Ajoute le libellé français du risque (ex: "Élevé - Consultation recommandée")
            result.put("risk_color", getRiskColor(riskLevel));  // Ajoute la couleur associée au risque (vert/orange/rouge)
            result.put("feedback_message", feedback.get("message"));  // Ajoute le message de feedback principal
            result.put("detailed_feedback", feedback.get("detailed"));  // Ajoute les feedbacks détaillés par catégorie
            result.put("recommendations", recommendations);  // Ajoute la liste des recommandations personnalisées
            result.put("disclaimer", "⚠️ Ce test est un outil de dépistage et non un diagnostic médical. Seul un professionnel de santé (psychologue ou psychiatre) peut poser un diagnostic.");  // Ajoute un avertissement légal important

            return ResponseEntity.ok(result);  // Retourne HTTP 200 (OK) avec tous les résultats en JSON

        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement
            log.error("Erreur lors de la soumission: {}", e.getMessage(), e);  // Log l'erreur complète avec stacktrace pour débogage
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne: " + e.getMessage()));  // Retourne HTTP 500 (Internal Server Error) avec message d'erreur
        }
    }

    // ===== ENDPOINT 3: HISTORIQUE DES TESTS =====
    @GetMapping("/{childId}/history")  // Associe les requêtes GET à "/api/behavioral-test/{childId}/history" à cette méthode
    public ResponseEntity<List<Map<String, Object>>> getTestHistory(  // Récupère l'historique complet des tests pour un enfant spécifique
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL
            @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié depuis le token JWT

        try {  // Bloc try-catch pour capturer et gérer les erreurs
            User user = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base par son email
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance exception si non trouvé
            
            Child child = childRepository.findById(childId)  // Cherche l'enfant en base par son ID
                    .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));  // Lance exception si non trouvé

            // VÉRIFICATION DES PERMISSIONS SELON LE RÔLE
            if (user.isParent() && (child.getParent() == null || !child.getParent().getId().equals(user.getId()))) {  // Vérifie parent autorisé
                log.warn("Parent non autorisé pour l'historique");  // Log d'avertissement
                return ResponseEntity.status(403).body(null);  // Retourne HTTP 403 Forbidden
            }
            
            if (user.isTeacher() && (child.getTeacher() == null || !child.getTeacher().getId().equals(user.getId()))) {  // Vérifie enseignant autorisé
                log.warn("Teacher non autorisé pour l'historique");  // Log d'avertissement
                return ResponseEntity.status(403).body(null);  // Retourne HTTP 403 Forbidden
            }
            
            if (user.isPsychologist() && (child.getPsychologist() == null || !child.getPsychologist().getId().equals(user.getId()))) {  // Vérifie psychologue autorisé
                log.warn("Psychologist non autorisé pour l'historique");  // Log d'avertissement
                return ResponseEntity.status(403).body(null);  // Retourne HTTP 403 Forbidden
            }

            // RÉCUPÉRATION DE L'HISTORIQUE
            List<BehavioralTest> tests = behavioralTestRepository.findByChildOrderByCreatedAtDesc(child);  // Récupère tous les tests de l'enfant (triés du plus récent au plus ancien)
            List<Map<String, Object>> history = new ArrayList<>();  // Crée une liste pour stocker l'historique formaté

            for (BehavioralTest test : tests) {  // Parcourt chaque test trouvé
                Map<String, Object> item = new HashMap<>();  // Crée une map pour un test individuel
                item.put("id", test.getId());  // Ajoute l'ID du test
                item.put("created_at", test.getCreatedAt());  // Ajoute la date et heure de création
                item.put("user_role", test.getUserRole());  // Ajoute le rôle de l'utilisateur qui a passé le test (parent/teacher)
                item.put("risk_level", test.getRiskLevel());  // Ajoute le niveau de risque (low/moderate/high)
                
                // Désérialisation des scores depuis JSON
                Map<String, Object> scores = objectMapper.readValue(test.getScores(), new TypeReference<Map<String, Object>>() {});  // Convertit le JSON des scores en Map Java
                item.put("scores", scores);  // Ajoute les scores à l'item
                
                history.add(item);  // Ajoute l'item à la liste d'historique
            }

            return ResponseEntity.ok(history);  // Retourne HTTP 200 avec la liste complète de l'historique en JSON

        } catch (Exception e) {  // Capture toute exception
            log.error("Erreur lors de la récupération de l'historique: {}", e.getMessage(), e);  // Log l'erreur
            return ResponseEntity.status(500).body(null);  // Retourne HTTP 500 (Internal Server Error)
        }
    }

    // ===== ENDPOINT 4: TEST SIMPLE =====
    @GetMapping("/test")  // Associe les requêtes GET à "/api/behavioral-test/test" à cette méthode
    public ResponseEntity<Map<String, String>> test() {  // Endpoint simple pour vérifier que le contrôleur fonctionne
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Test endpoint works"));  // Retourne HTTP 200 avec un statut OK et un message de confirmation
    }

    // ===== MÉTHODE DE CALCUL DES SCORES =====
    private Map<String, Object> calculateScores(Map<String, String> responses) {  // Calcule les scores pour chaque catégorie et le score global
        Map<String, Object> scores = new HashMap<>();  // Crée une map pour stocker tous les scores
        Map<String, Object> overall = new HashMap<>();  // Crée une map pour le score global
        int overallTotal = 0;  // Initialise le total global des scores à 0
        int overallMax = 100;  // Score maximum global (4 catégories x 25 points maximum par catégorie)

        Map<String, List<Map<String, Object>>> questions = buildQuestions();  // Récupère la structure complète des questions (pour connaître les poids)

        for (Map.Entry<String, List<Map<String, Object>>> categoryEntry : questions.entrySet()) {  // Parcourt chaque catégorie (communication, social, attention, behavior)
            String category = categoryEntry.getKey();  // Récupère le nom de la catégorie (ex: "communication")
            List<Map<String, Object>> categoryQuestions = categoryEntry.getValue();  // Récupère la liste des 5 questions de cette catégorie

            int categoryTotal = 0;  // Initialise le score total de la catégorie à 0
            int categoryMax = 25;  // Score maximum par catégorie (5 questions x 5 points max par question)

            for (int i = 0; i < categoryQuestions.size(); i++) {  // Parcourt chaque question de la catégorie (index 0 à 4)
                Map<String, Object> q = categoryQuestions.get(i);  // Récupère la question à l'index i
                String responseKey = category + "_" + (i + 1);  // Construit la clé de réponse (ex: "communication_1", "social_2")
                String answer = responses.get(responseKey);  // Récupère la réponse de l'utilisateur pour cette question
                
                if (answer != null) {  // Si une réponse existe pour cette question
                    @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast générique
                    Map<String, Integer> weights = (Map<String, Integer>) q.get("weights");  // Récupère la map des poids pour cette question
                    Integer weight = weights.get(answer);  // Récupère le poids associé à la réponse choisie
                    
                    if (weight != null) {  // Si le poids existe (vérification de sécurité)
                        categoryTotal += weight;  // Ajoute le poids au total de la catégorie
                    }
                }
            }

            double percentage = (double) categoryTotal / categoryMax * 100;  // Calcule le pourcentage (score obtenu / score max * 100)
            Map<String, Object> categoryScore = new HashMap<>();  // Crée une map pour stocker le score de la catégorie
            categoryScore.put("total", categoryTotal);  // Ajoute le total obtenu
            categoryScore.put("max", categoryMax);  // Ajoute le maximum possible
            categoryScore.put("percentage", Math.round(percentage * 10) / 10.0);  // Ajoute le pourcentage arrondi à 1 décimale
            scores.put(category, categoryScore);  // Ajoute le score de la catégorie à la map des scores

            overallTotal += categoryTotal;  // Ajoute le total de cette catégorie au total global
        }

        double overallPercentage = (double) overallTotal / overallMax * 100;  // Calcule le pourcentage global (total obtenu / total max * 100)
        overall.put("total", overallTotal);  // Ajoute le total global obtenu
        overall.put("max", overallMax);  // Ajoute le maximum global possible
        overall.put("percentage", Math.round(overallPercentage * 10) / 10.0);  // Ajoute le pourcentage global arrondi à 1 décimale
        scores.put("overall", overall);  // Ajoute le score global à la map des scores

        return scores;  // Retourne tous les scores calculés (catégories + global)
    }

    private String determineRiskLevel(Map<String, Object> overallScore) {  // Détermine le niveau de risque à partir du score global
        double percentage = (double) overallScore.get("percentage");  // Récupère le pourcentage global
        if (percentage >= 60) return "high";  // Si score >= 60% : risque élevé (consultation recommandée)
        if (percentage >= 35) return "moderate";  // Si score entre 35% et 60% : risque modéré (surveillance recommandée)
        return "low";  // Si score < 35% : risque faible (développement typique)
    }

    private String getRiskLabel(String riskLevel) {  // Retourne le libellé français du niveau de risque (pour affichage frontend)
        return switch (riskLevel) {  // Switch expression Java 14+
            case "low" -> "Faible - Préoccupations mineures";  // Libellé pour risque faible
            case "moderate" -> "Modéré - Surveillance recommandée";  // Libellé pour risque modéré
            case "high" -> "Élevé - Consultation recommandée";  // Libellé pour risque élevé
            default -> riskLevel;  // Valeur par défaut (au cas où)
        };
    }

    private String getRiskColor(String riskLevel) {  // Retourne la couleur hexadécimale associée au niveau de risque (pour affichage frontend)
        return switch (riskLevel) {  // Switch expression
            case "low" -> "#10b981";  // Vert pour risque faible
            case "moderate" -> "#f59e0b";  // Orange pour risque modéré
            case "high" -> "#ef4444";  // Rouge pour risque élevé
            default -> "#6b7280";  // Gris par défaut
        };
    }

    private Map<String, Object> generateFeedback(Map<String, Object> scores, String riskLevel) {  // Génère des feedbacks personnalisés (global et détaillés par catégorie)
        Map<String, Object> feedback = new HashMap<>();  // Crée une map pour stocker les feedbacks
        List<Map<String, Object>> detailed = new ArrayList<>();  // Crée une liste pour stocker les feedbacks détaillés par catégorie

        String message;  // Déclare le message principal (global)
        
        // Sélection du message principal selon le niveau de risque
        switch (riskLevel) {  // Switch sur le niveau de risque
            case "low":  // Cas risque faible
                message = "🌟 Les résultats sont rassurants ! Votre enfant montre des comportements typiques pour son âge. Continuez à l'encourager et à suivre son développement.";  // Message positif et encourageant
                break;  // Sortie du switch
                
            case "moderate":  // Cas risque modéré
                message = "📊 Certains comportements méritent une attention particulière. Une surveillance régulière et la mise en place de routines adaptées pourraient être bénéfiques.";  // Message de vigilance proactive
                break;  // Sortie du switch
                
            default:  // Cas risque élevé (ou valeur par défaut)
                message = "⚠️ Les résultats suggèrent des difficultés significatives qui justifient une évaluation par un professionnel de santé (psychologue ou psychiatre).";  // Message d'alerte avec recommandation professionnelle
                break;  // Sortie du switch
        }

        Map<String, String> categoryNames = Map.of(  // Map associant les codes catégorie à leurs libellés français pour les feedbacks
                "communication", "la communication",  // Code "communication" -> "la communication"
                "social", "les interactions sociales",  // Code "social" -> "les interactions sociales"
                "attention", "l'attention",  // Code "attention" -> "l'attention"
                "behavior", "le comportement"  // Code "behavior" -> "le comportement"
        );

        for (Map.Entry<String, Object> entry : scores.entrySet()) {  // Parcourt chaque catégorie de score (communication, social, attention, behavior)
            String category = entry.getKey();  // Récupère le nom de la catégorie
            if ("overall".equals(category)) continue;  // Ignore le score global (ne pas générer de feedback détaillé pour le global)

            @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast générique
            Map<String, Object> scoreData = (Map<String, Object>) entry.getValue();  // Cast la valeur en Map
            double percentage = (double) scoreData.get("percentage");  // Récupère le pourcentage pour cette catégorie

            Map<String, Object> detail = new HashMap<>();  // Crée une map pour le feedback détaillé de cette catégorie
            detail.put("category", category);  // Ajoute le nom de la catégorie

            // Sélection du message selon le score de la catégorie
            if (percentage >= 60) {  // Si score élevé dans cette catégorie (≥ 60%)
                detail.put("level", "high");  // Niveau élevé
                detail.put("message", "⚠️ Difficultés importantes en " + categoryNames.get(category) + " : des interventions ciblées sont recommandées.");  // Message d'alerte spécifique à la catégorie
            } else if (percentage >= 35) {  // Si score modéré (35% à 60%)
                detail.put("level", "moderate");  // Niveau modéré
                detail.put("message", "📌 Difficultés modérées en " + categoryNames.get(category) + " : une attention particulière est conseillée.");  // Message de vigilance spécifique
            } else {  // Si score faible (< 35%)
                detail.put("level", "low");  // Niveau faible
                detail.put("message", "✅ Comportements typiques en " + categoryNames.get(category) + ".");  // Message positif spécifique
            }
            detailed.add(detail);  // Ajoute le feedback détaillé à la liste
        }

        feedback.put("message", message);  // Ajoute le message principal à la map des feedbacks
        feedback.put("detailed", detailed);  // Ajoute la liste des feedbacks détaillés
        return feedback;  // Retourne la map des feedbacks (global + détaillés)
    }

    private List<String> generateRecommendations(Map<String, Object> scores, String riskLevel) {  // Génère des recommandations personnalisées basées sur les scores
        List<String> recommendations = new ArrayList<>();  // Crée une liste pour stocker les recommandations (sans doublons)

        // RECOMMANDATIONS POUR RISQUE ÉLEVÉ
        if ("high".equals(riskLevel)) {  // Si le risque global est élevé
            recommendations.add("👩‍⚕️ Consultez un psychologue spécialisé pour une évaluation complète");  // Recommandation consultation professionnelle
            recommendations.add("📝 Tenez un journal quotidien des comportements pour partager avec le professionnel");  // Recommandation journal de bord
            recommendations.add("🏫 Informez l'école et demandez un accompagnement adapté si nécessaire");  // Recommandation coordination scolaire
        }

        // RECOMMANDATIONS POUR RISQUE MODÉRÉ OU ÉLEVÉ (basées sur les scores par catégorie)
        if ("moderate".equals(riskLevel) || "high".equals(riskLevel)) {  // Si risque modéré ou élevé
            @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast
            Map<String, Object> communication = (Map<String, Object>) scores.get("communication");  // Récupère le score de communication
            @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast
            Map<String, Object> social = (Map<String, Object>) scores.get("social");  // Récupère le score social
            @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast
            Map<String, Object> attention = (Map<String, Object>) scores.get("attention");  // Récupère le score d'attention
            @SuppressWarnings("unchecked")  // Supprime l'avertissement de cast
            Map<String, Object> behavior = (Map<String, Object>) scores.get("behavior");  // Récupère le score de comportement

            // RECOMMANDATIONS SPÉCIFIQUES COMMUNICATION
            if (communication != null && (double) communication.get("percentage") >= 35) {  // Si difficultés en communication
                recommendations.add("💬 Encouragez les interactions verbales par des jeux de rôle et la lecture d'histoires");  // Recommandation jeux de rôle
                recommendations.add("🗣️ Utilisez des images et des pictogrammes pour faciliter la communication");  // Recommandation supports visuels
            }

            // RECOMMANDATIONS SPÉCIFIQUES SOCIAL
            if (social != null && (double) social.get("percentage") >= 35) {  // Si difficultés sociales
                recommendations.add("👥 Organisez des jeux avec un seul camarade à la fois pour faciliter les interactions");  // Recommandation interactions individuelles
                recommendations.add("🎭 Apprenez les règles sociales à travers des histoires sociales");  // Recommandation histoires sociales
            }

            // RECOMMANDATIONS SPÉCIFIQUES ATTENTION
            if (attention != null && (double) attention.get("percentage") >= 35) {  // Si difficultés d'attention
                recommendations.add("⏱️ Proposez des activités courtes (10-15 minutes) avec des objectifs clairs");  // Recommandation activités courtes
                recommendations.add("🏆 Utilisez un système de récompenses visuel pour maintenir la motivation");  // Recommandation système de récompenses
                recommendations.add("🧘 Introduisez des pauses actives entre les activités");  // Recommandation pauses actives
            }

            // RECOMMANDATIONS SPÉCIFIQUES COMPORTEMENT
            if (behavior != null && (double) behavior.get("percentage") >= 35) {  // Si difficultés comportementales
                recommendations.add("📅 Établissez des routines visuelles pour sécuriser l'enfant");  // Recommandation routines visuelles
                recommendations.add("🎵 Utilisez la musique ou des minuteurs pour faciliter les transitions");  // Recommandation outils de transition
                recommendations.add("😌 Créez un coin calme avec des outils sensoriels (coussin, sable, pâte à modeler)");  // Recommandation espace sensoriel
            }
        }

        // RECOMMANDATIONS GÉNÉRALES (POUR TOUS)
        recommendations.add("🥗 Veillez à une alimentation équilibrée et un sommeil régulier");  // Recommandation hygiène de vie
        recommendations.add("🏃‍♂️ Encouragez l'activité physique quotidienne (au moins 30 minutes)");  // Recommandation activité physique
        recommendations.add("⭐ Félicitez les efforts, pas seulement les résultats");  // Recommandation encouragement positif

        return recommendations.stream().distinct().toList();  // Supprime les doublons éventuels et retourne la liste finale
    }
}