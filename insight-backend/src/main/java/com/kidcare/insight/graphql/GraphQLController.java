package com.kidcare.insight.graphql;  // Déclare le package contenant les contrôleurs GraphQL

import com.kidcare.insight.dto.*;  // Importe tous les DTO (Data Transfer Objects)
import com.kidcare.insight.entity.*;  // Importe toutes les entités JPA
import com.kidcare.insight.repository.*;  // Importe tous les repositories JPA
import com.kidcare.insight.service.*;  // Importe tous les services métier
import com.kidcare.insight.config.JwtUtil;  // Importe l'utilitaire JWT pour la génération de tokens
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.graphql.data.method.annotation.*;  // Importe les annotations GraphQL (QueryMapping, MutationMapping, Argument)
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.stereotype.Controller;  // Importe l'annotation pour déclarer un contrôleur Spring

import java.time.LocalDate;  // Importe la classe LocalDate pour les dates
import java.util.*;  // Importe toutes les collections Java
import java.util.stream.Collectors;  // Importe la classe Collectors pour les streams

@Controller  // Déclare cette classe comme un contrôleur Spring (pour GraphQL)
public class GraphQLController {  // Déclare le contrôleur GraphQL qui expose une API alternative à REST

    // ===== INJECTION DE TOUS LES SERVICES ET REPOSITORIES =====
    @Autowired private UserService userService;  // Service pour la gestion des utilisateurs
    @Autowired private ChildService childService;  // Service pour la gestion des enfants
    @Autowired private BehaviorService behaviorService;  // Service pour les journaux de comportement
    @Autowired private InsightService insightService;  // Service pour les analyses statistiques
    @Autowired private NotificationService notificationService;  // Service pour les notifications
    @Autowired private MessageService messageService;  // Service pour la messagerie
    @Autowired private RoutineService routineService;  // Service pour les routines
    @Autowired private RecommendationService recommendationService;  // Service pour les recommandations
    @Autowired private PsychologistNoteService noteService;  // Service pour les notes des psychologues
    @Autowired private ActionPlanService actionPlanService;  // Service pour les plans d'action
    @Autowired private AppointmentService appointmentService;  // Service pour les rendez-vous
    @Autowired private ProfileService profileService;  // Service pour la gestion du profil
    @Autowired private JwtUtil jwtUtil;  // Utilitaire pour la gestion des tokens JWT
    @Autowired private UserRepository userRepository;  // Repository pour les utilisateurs
    @Autowired private MessageRepository messageRepository;  // Repository pour les messages
    @Autowired private RecommendationRepository recommendationRepository;  // Repository pour les recommandations

    // Méthode utilitaire pour diviser une chaîne d'activités (séparées par "||") en liste
    private List<String> splitActivities(String activities) {
        if (activities == null || activities.isEmpty()) {  // Vérifie si la chaîne est vide ou nulle
            return List.of();  // Retourne une liste vide
        }
        return Arrays.stream(activities.split("\\|\\|"))  // Divise la chaîne par "||"
                .map(String::trim)  // Supprime les espaces au début et à la fin
                .filter(s -> !s.isEmpty())  // Filtre les chaînes vides
                .collect(Collectors.toList());  // Collecte le résultat dans une liste
    }

    // Helper pour normaliser les clés RoutineInput GraphQL (camelCase → snake_case)
    private Map<String, Object> normalizeRoutineInput(Map<String, Object> input) {
        Map<String, Object> normalized = new HashMap<>(input);  // Crée une copie de la map d'entrée
        if (input.containsKey("dayOfWeek") && !input.containsKey("day_of_week")) {  // Vérifie si dayOfWeek existe mais pas day_of_week
            normalized.put("day_of_week", input.get("dayOfWeek"));  // Convertit camelCase en snake_case
        }
        return normalized;  // Retourne la map normalisée
    }

    // ===== QUERIES GRAPHQL (LECTURE) =====

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public User me(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur actuellement connecté
        return userService.findByEmail(userDetails.getUsername());  // Retourne l'utilisateur complet
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Child> children(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère tous les enfants de l'utilisateur
        return childService.getChildrenForUser(userDetails.getUsername());  // Retourne la liste des enfants
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public Child child(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère un enfant spécifique par son ID
        return childService.getChildById(Long.parseLong(id), userDetails.getUsername());  // Appelle le service avec l'ID converti en Long
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<BehaviorLog> logs(@Argument String childId, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère tous les logs d'un enfant
        return behaviorService.getLogsForChild(Long.parseLong(childId), userDetails.getUsername());  // Appelle le service
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public Map<String, Object> insights(@Argument String childId) {  // Récupère les analyses statistiques pour un enfant
        return insightService.getInsights(Long.parseLong(childId));  // Retourne les insights (sans authentification car gérée par SecurityConfig)
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Notification> notifications(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère toutes les notifications de l'utilisateur
        return notificationService.getNotificationsForUser(userDetails.getUsername());  // Appelle le service
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public long unreadNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère le nombre de notifications non lues
        return notificationService.getUnreadCount(userDetails.getUsername());  // Retourne le compteur
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Map<String, Object>> contacts(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère la liste des contacts pour la messagerie
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur courant
        List<User> contactUsers = new ArrayList<>();  // Crée une liste pour les contacts

        // Sélection des contacts selon le rôle de l'utilisateur
        if (currentUser.isParent()) {  // Si l'utilisateur est un parent
            contactUsers.addAll(userRepository.findByRole("teacher"));  // Ajoute tous les enseignants
            contactUsers.addAll(userRepository.findByRole("psychologist"));  // Ajoute tous les psychologues
        } else if (currentUser.isTeacher()) {  // Si l'utilisateur est un enseignant
            contactUsers.addAll(userRepository.findByRole("parent"));  // Ajoute tous les parents
            contactUsers.addAll(userRepository.findByRole("psychologist"));  // Ajoute tous les psychologues
        } else if (currentUser.isPsychologist()) {  // Si l'utilisateur est un psychologue
            contactUsers.addAll(userRepository.findByRole("parent"));  // Ajoute tous les parents
            contactUsers.addAll(userRepository.findByRole("teacher"));  // Ajoute tous les enseignants
        }

        // Transformation de chaque contact en map avec informations supplémentaires
        return contactUsers.stream().distinct().map(contact -> {  // Élimine les doublons et transforme
            Map<String, Object> c = new HashMap<>();  // Crée une map pour le contact
            c.put("id", contact.getId());  // Ajoute l'ID
            c.put("name", contact.getName());  // Ajoute le nom
            c.put("email", contact.getEmail());  // Ajoute l'email
            c.put("role", contact.getRole().toLowerCase());  // Ajoute le rôle en minuscules

            List<Message> msgs = messageRepository.findConversation(currentUser.getId(), contact.getId());  // Récupère la conversation
            if (!msgs.isEmpty()) {  // Si des messages existent
                c.put("lastMessage", msgs.get(msgs.size() - 1).getContent());  // Ajoute le dernier message
            } else {
                c.put("lastMessage", null);  // Sinon, met null
            }
            c.put("unreadCount", messageRepository.countByReceiverIdAndIsReadFalse(currentUser.getId()));  // Ajoute le compteur de non lus
            return c;  // Retourne la map du contact
        }).collect(Collectors.toList());  // Collecte dans une liste
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Message> messages(@Argument String userId, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère la conversation avec un utilisateur
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur courant
        User otherUser = userRepository.findById(Long.parseLong(userId)).orElseThrow();  // Charge l'autre utilisateur

        List<Message> msgs = messageService.getConversation(currentUser.getId(), otherUser.getId());  // Récupère la conversation
        
        for (Message msg : msgs) {  // Parcourt chaque message
            if (msg.getReceiver().getId().equals(currentUser.getId()) && !msg.getIsRead()) {  // Si le message est destiné à l'utilisateur courant ET non lu
                msg.setIsRead(true);  // Marque comme lu
                messageRepository.save(msg);  // Sauvegarde
            }
        }
        return msgs;  // Retourne la liste des messages
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public long unreadMessageCount(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère le nombre total de messages non lus
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur courant
        return messageRepository.countByReceiverAndIsReadFalse(currentUser);  // Retourne le compteur
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<User> psychologists() {  // Récupère la liste de tous les psychologues
        return userRepository.findByRole("psychologist");  // Retourne la liste
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<User> teachers() {  // Récupère la liste de tous les enseignants
        return userRepository.findByRole("teacher");  // Retourne la liste
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<User> parents() {  // Récupère la liste de tous les parents
        return userRepository.findByRole("parent");  // Retourne la liste
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<RoutineDTO> routines(@Argument String childId, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère toutes les routines d'un enfant
        System.out.println("=== GraphQL routines query ===");  // Log de débogage
        System.out.println("Child ID: " + childId);
        System.out.println("User: " + userDetails.getUsername());
        
        List<RoutineDTO> result = routineService.getRoutinesForChild(Long.parseLong(childId), userDetails.getUsername());  // Appelle le service
        
        System.out.println("Routines found: " + result.size());  // Log du nombre trouvé
        for (RoutineDTO r : result) {  // Log chaque routine
            System.out.println("  - Routine: id=" + r.getId() + 
                             ", dayOfWeek=" + r.getDayOfWeek() + 
                             ", activity=" + r.getActivity() +
                             ", completed=" + r.getCompleted());
        }
        return result;  // Retourne la liste des routines
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Recommendation> recommendations(@Argument String childId, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère toutes les recommandations d'un enfant
        return recommendationService.getRecommendationsForChild(Long.parseLong(childId), userDetails.getUsername());  // Appelle le service
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<PsychologistNote> notes(@Argument String childId, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère toutes les notes d'un psychologue pour un enfant
        return noteService.getNotesForChild(Long.parseLong(childId), userDetails.getUsername());  // Appelle le service
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public Map<String, Object> latestActionPlan(@Argument String childId) {  // Récupère le dernier plan d'action pour un enfant
        ActionPlan plan = actionPlanService.getLatestPlan(Long.parseLong(childId));  // Récupère le plan
        if (plan == null) return null;  // Si aucun plan, retourne null

        Map<String, Object> result = new HashMap<>();  // Crée une map pour la réponse
        result.put("id", plan.getId());  // Ajoute l'ID
        result.put("riskLevel", plan.getRiskLevel());  // Ajoute le niveau de risque
        result.put("generatedDate", plan.getGeneratedDate());  // Ajoute la date de génération
        result.put("morningActivities", splitActivities(plan.getMorningActivities()));  // Divise les activités du matin en liste
        result.put("afternoonActivities", splitActivities(plan.getAfternoonActivities()));  // Divise les activités de l'après-midi
        result.put("eveningActivities", splitActivities(plan.getEveningActivities()));  // Divise les activités du soir
        result.put("communicationTips", splitActivities(plan.getCommunicationTips()));  // Divise les conseils de communication
        result.put("gamesActivities", splitActivities(plan.getGamesActivities()));  // Divise les jeux recommandés

        return result;  // Retourne la map
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public List<Appointment> appointments(@AuthenticationPrincipal UserDetails userDetails) {  // Récupère tous les rendez-vous de l'utilisateur
        return appointmentService.getAppointmentsForUser(userDetails.getUsername());  // Appelle le service
    }

    @QueryMapping  // Déclare cette méthode comme une requête GraphQL
    public Appointment appointment(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Récupère un rendez-vous spécifique par ID
        return appointmentService.getAppointmentById(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
    }

    // ===== MUTATIONS GRAPHQL (ÉCRITURE) =====

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Map<String, Object> login(@Argument String email, @Argument String password) {  // Connecte un utilisateur
        User user = userService.findByEmail(email);  // Cherche l'utilisateur par email
        if (user == null) throw new RuntimeException("Invalid credentials");  // Si non trouvé, erreur
        String token = jwtUtil.generateToken(email);  // Génère un token JWT
        Map<String, Object> result = new HashMap<>();  // Crée une map pour la réponse
        result.put("token", token);  // Ajoute le token
        result.put("user", user);  // Ajoute l'utilisateur
        return result;  // Retourne la réponse
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Map<String, Object> register(@Argument String name, @Argument String email,
                                        @Argument String password, @Argument String role) {  // Inscrit un nouvel utilisateur
        User user = userService.register(name, email, password, role);  // Crée l'utilisateur
        String token = jwtUtil.generateToken(email);  // Génère un token JWT
        Map<String, Object> result = new HashMap<>();  // Crée une map pour la réponse
        result.put("token", token);  // Ajoute le token
        result.put("user", user);  // Ajoute l'utilisateur
        return result;  // Retourne la réponse
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Child createChild(@Argument Map<String, Object> input, @AuthenticationPrincipal UserDetails userDetails) {  // Crée un nouvel enfant
        ChildRequest req = mapToChildRequest(input);  // Convertit la map en ChildRequest
        return childService.createChild(req, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Child updateChild(@Argument String id, @Argument Map<String, Object> input,
                             @AuthenticationPrincipal UserDetails userDetails) {  // Met à jour un enfant existant
        ChildRequest req = mapToChildRequest(input);  // Convertit la map en ChildRequest
        return childService.updateChild(Long.parseLong(id), req, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteChild(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime un enfant
        childService.deleteChild(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public BehaviorLog createLog(@Argument Map<String, Object> input, @AuthenticationPrincipal UserDetails userDetails) {  // Crée un journal de comportement
        BehaviorLogRequest req = new BehaviorLogRequest();  // Crée une nouvelle requête
        req.setChildId(Long.parseLong(input.get("childId").toString()));  // Définit l'ID de l'enfant
        req.setFocusLevel((Integer) input.get("focusLevel"));  // Définit le niveau de concentration
        req.setMood((String) input.get("mood"));  // Définit l'humeur
        req.setSleepHours(Double.parseDouble(input.get("sleepHours").toString()));  // Définit les heures de sommeil
        req.setSocialInteraction((Integer) input.get("socialInteraction"));  // Définit l'interaction sociale
        req.setNote((String) input.getOrDefault("note", null));  // Définit la note (si présente)
        req.setLogDate(LocalDate.parse((String) input.get("logDate")));  // Définit la date du log
        return behaviorService.createLog(req, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteLog(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime un journal de comportement
        behaviorService.deleteLog(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Message sendMessage(@Argument String receiverId, @Argument String content,
                               @AuthenticationPrincipal UserDetails userDetails) {  // Envoie un message
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'expéditeur
        User receiver = userRepository.findById(Long.parseLong(receiverId)).orElseThrow();  // Charge le destinataire
        return messageService.sendMessage(currentUser.getId(), receiver.getId(), content);  // Envoie le message
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Message updateMessage(@Argument String id, @Argument String content,
                                 @AuthenticationPrincipal UserDetails userDetails) {  // Modifie un message existant
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur
        Message msg = messageRepository.findById(Long.parseLong(id)).orElseThrow();  // Cherche le message
        if (!msg.getSender().getId().equals(currentUser.getId())) {  // Vérifie si l'utilisateur est l'auteur
            throw new RuntimeException("Unauthorized");  // Si non, erreur
        }
        msg.setContent(content);  // Modifie le contenu
        return messageRepository.save(msg);  // Sauvegarde et retourne
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteMessage(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime un message
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur
        Message msg = messageRepository.findById(Long.parseLong(id)).orElseThrow();  // Cherche le message
        if (!msg.getSender().getId().equals(currentUser.getId())) {  // Vérifie si l'utilisateur est l'auteur
            throw new RuntimeException("Unauthorized");  // Si non, erreur
        }
        messageRepository.delete(msg);  // Supprime le message
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteConversation(@Argument String userId, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime toute une conversation
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur courant
        User otherUser = userRepository.findById(Long.parseLong(userId)).orElseThrow();  // Charge l'autre utilisateur
        List<Message> msgs = messageService.getConversation(currentUser.getId(), otherUser.getId());  // Récupère la conversation
        messageRepository.deleteAll(msgs);  // Supprime tous les messages
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Message markMessageAsRead(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Marque un message comme lu
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur
        Message msg = messageRepository.findById(Long.parseLong(id)).orElseThrow();  // Cherche le message
        if (msg.getReceiver().getId().equals(currentUser.getId())) {  // Vérifie si l'utilisateur est le destinataire
            msg.setIsRead(true);  // Marque comme lu
            return messageRepository.save(msg);  // Sauvegarde et retourne
        }
        throw new RuntimeException("Unauthorized");  // Sinon, erreur
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean markAllMessagesAsRead(@Argument String userId, @AuthenticationPrincipal UserDetails userDetails) {  // Marque tous les messages d'une conversation comme lus
        User currentUser = userService.findByEmail(userDetails.getUsername());  // Charge l'utilisateur courant
        User sender = userRepository.findById(Long.parseLong(userId)).orElseThrow();  // Charge l'expéditeur
        messageService.markMessagesAsRead(currentUser.getId(), sender.getId());  // Marque comme lus
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean markNotificationAsRead(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Marque une notification comme lue
        notificationService.markAsRead(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean markAllNotificationsAsRead(@AuthenticationPrincipal UserDetails userDetails) {  // Marque toutes les notifications comme lues
        notificationService.markAllAsRead(userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteNotification(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime une notification
        notificationService.deleteNotification(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteAllNotifications(@AuthenticationPrincipal UserDetails userDetails) {  // Supprime toutes les notifications
        notificationService.deleteAllNotifications(userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Recommendation addRecommendation(@Argument String childId, @Argument Map<String, Object> input,
                                            @AuthenticationPrincipal UserDetails userDetails) {  // Ajoute une recommandation
        RecommendationRequest req = new RecommendationRequest();  // Crée une nouvelle requête
        req.setTitle((String) input.get("title"));  // Définit le titre
        req.setDescription((String) input.get("description"));  // Définit la description
        req.setCategory((String) input.get("category"));  // Définit la catégorie
        return recommendationService.addRecommendation(Long.parseLong(childId), req, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Recommendation toggleRecommendation(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Inverse l'état de complétion d'une recommandation
        Recommendation rec = recommendationRepository.findById(Long.parseLong(id)).orElseThrow();  // Cherche la recommandation
        rec.setCompleted(!rec.isCompleted());  // Inverse l'état
        return recommendationRepository.save(rec);  // Sauvegarde et retourne
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteRecommendation(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime une recommandation
        recommendationService.deleteRecommendation(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public PsychologistNote addNote(@Argument String childId, @Argument Map<String, Object> input,
                                    @AuthenticationPrincipal UserDetails userDetails) {  // Ajoute une note de psychologue
        NoteRequest req = new NoteRequest();  // Crée une nouvelle requête
        req.setNote((String) input.get("note"));  // Définit le contenu de la note
        req.setSessionDate(LocalDate.parse((String) input.get("sessionDate")));  // Définit la date de session
        return noteService.addNote(Long.parseLong(childId), req, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public RoutineDTO createRoutine(@Argument String childId, @Argument Map<String, Object> input,
                                    @AuthenticationPrincipal UserDetails userDetails) {  // Crée une routine
        System.out.println("=== GraphQL createRoutine ===");  // Log de débogage
        System.out.println("Child ID: " + childId);
        System.out.println("Input: " + input);
        return routineService.createRoutine(Long.parseLong(childId), normalizeRoutineInput(input), userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public RoutineDTO updateRoutine(@Argument String id, @Argument Map<String, Object> input,
                                    @AuthenticationPrincipal UserDetails userDetails) {  // Met à jour une routine
        System.out.println("=== GraphQL updateRoutine ===");  // Log de débogage
        System.out.println("Routine ID: " + id);
        System.out.println("Input: " + input);
        return routineService.updateRoutine(Long.parseLong(id), normalizeRoutineInput(input), userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public RoutineDTO toggleRoutine(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Inverse l'état de complétion d'une routine
        System.out.println("=== GraphQL toggleRoutine ===");  // Log de débogage
        System.out.println("Routine ID: " + id);
        return routineService.toggleComplete(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteRoutine(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime une routine
        System.out.println("=== GraphQL deleteRoutine ===");  // Log de débogage
        System.out.println("Routine ID: " + id);
        routineService.deleteRoutine(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public ActionPlan generateActionPlan(@Argument String childId) {  // Génère un plan d'action pour un enfant
        return actionPlanService.generatePlan(Long.parseLong(childId));  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public User updateProfile(@Argument String name, @Argument String email,
                              @AuthenticationPrincipal UserDetails userDetails) {  // Met à jour le profil utilisateur
        return profileService.updateProfile(userDetails.getUsername(), name, email);  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean updatePassword(@Argument String currentPassword, @Argument String newPassword,
                                  @Argument String passwordConfirmation,
                                  @AuthenticationPrincipal UserDetails userDetails) {  // Change le mot de passe
        profileService.updatePassword(userDetails.getUsername(), currentPassword, newPassword);  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteAccount(@Argument String password, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime le compte utilisateur
        profileService.deleteAccount(userDetails.getUsername(), password);  // Appelle le service
        return true;  // Retourne true si succès
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Appointment createAppointment(@Argument Map<String, Object> input,
                                         @AuthenticationPrincipal UserDetails userDetails) {  // Crée un rendez-vous
        Map<String, Object> data = new HashMap<>(input);  // Copie la map d'entrée
        data.put("scheduled_at", input.get("scheduledAt"));  // Convertit scheduledAt en scheduled_at (compatibilité)
        return appointmentService.createAppointment(data, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public Appointment updateAppointmentStatus(@Argument String id, @Argument String status,
                                               @AuthenticationPrincipal UserDetails userDetails) {  // Met à jour le statut d'un rendez-vous
        return appointmentService.updateStatus(Long.parseLong(id), status, userDetails.getUsername());  // Appelle le service
    }

    @MutationMapping  // Déclare cette méthode comme une mutation GraphQL
    public boolean deleteAppointment(@Argument String id, @AuthenticationPrincipal UserDetails userDetails) {  // Supprime un rendez-vous
        appointmentService.deleteAppointment(Long.parseLong(id), userDetails.getUsername());  // Appelle le service
        return true;  // Retourne true si succès
    }

    // Méthode utilitaire privée pour convertir une Map<String, Object> en ChildRequest
    private ChildRequest mapToChildRequest(Map<String, Object> input) {
        ChildRequest req = new ChildRequest();  // Crée une nouvelle requête
        req.setName((String) input.get("name"));  // Définit le nom
        req.setAge((Integer) input.get("age"));  // Définit l'âge
        req.setNotes((String) input.getOrDefault("notes", null));  // Définit les notes (si présente)
        if (input.get("parentId") != null)  // Si parentId est présent
            req.setParentId(Long.parseLong(input.get("parentId").toString()));  // Définit parentId
        if (input.get("psychologistId") != null)  // Si psychologistId est présent
            req.setPsychologistId(Long.parseLong(input.get("psychologistId").toString()));  // Définit psychologistId
        if (input.get("teacherId") != null)  // Si teacherId est présent
            req.setTeacherId(Long.parseLong(input.get("teacherId").toString()));  // Définit teacherId
        return req;  // Retourne la requête
    }
}