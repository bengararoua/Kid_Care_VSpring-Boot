package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.NoteRequest;  // Importe l'objet de transfert contenant les données d'une note (contenu, date de session)
import com.kidcare.insight.entity.PsychologistNote;  // Importe l'entité représentant une note rédigée par un psychologue pour un enfant
import com.kidcare.insight.service.PsychologistNoteService;  // Importe le service contenant la logique métier pour les notes des psychologues
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.HttpStatus;  // Importe les codes de statut HTTP (201 Created, 401 Unauthorized, etc.)
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PostMapping, DeleteMapping, CrossOrigin)

import java.util.HashMap;  // Importe la classe HashMap pour créer des map clé-valeur
import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.Map;  // Importe l'interface Map pour manipuler des collections clé-valeur

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api")  // Définit le préfixe commun "/api" pour tous les endpoints de ce contrôleur
@CrossOrigin(origins = "http://localhost:3000")  // Autorise les requêtes cross-origin provenant du frontend React (port 3000)
public class PsychologistNoteController {  // Déclare le contrôleur gérant les notes rédigées par les psychologues pour les enfants

    @Autowired  // Injecte automatiquement le bean PsychologistNoteService géré par Spring
    private PsychologistNoteService psychologistNoteService;  // Service contenant la logique métier pour les notes des psychologues

    @GetMapping("/children/{childId}/notes")  // Associe les requêtes GET à "/api/children/{childId}/notes" à cette méthode
    public ResponseEntity<?> getNotes(  // Récupère toutes les notes d'un enfant (accessibles par le parent, le psychologue ou l'enseignant)
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /children/5/notes)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🔵 GET /children/" + childId + "/notes");  // Log de débogage (affiche l'appel)
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            List<PsychologistNote> notes = psychologistNoteService.getNotesForChild(childId, user.getUsername());  // Appelle le service pour récupérer toutes les notes de l'enfant (vérifie les droits)
            
            List<Map<String, Object>> response = new java.util.ArrayList<>();  // Crée une liste pour stocker les notes formatées en map
            for (PsychologistNote note : notes) {  // Parcourt chaque note récupérée
                Map<String, Object> noteMap = new HashMap<>();  // Crée une map pour une note individuelle
                noteMap.put("id", note.getId());  // Ajoute l'ID de la note
                noteMap.put("note", note.getNote());  // Ajoute le contenu texte de la note
                noteMap.put("sessionDate", note.getSessionDate() != null ? note.getSessionDate().toString() : null);  // Ajoute la date de la session (ou null si absente)
                noteMap.put("createdAt", note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);  // Ajoute la date de création (ou null si absente)
                noteMap.put("psychologistId", note.getPsychologist() != null ? note.getPsychologist().getId() : null);  // Ajoute l'ID du psychologue qui a écrit la note
                noteMap.put("psychologistName", note.getPsychologist() != null ? note.getPsychologist().getName() : null);  // Ajoute le nom du psychologue
                response.add(noteMap);  // Ajoute la note formatée à la liste de réponse
            }
            
            return ResponseEntity.ok(response);  // Retourne HTTP 200 (OK) avec la liste des notes en JSON

        } catch (Exception e) {  // Capture toute exception (erreur de permission, enfant non trouvé, etc.)
            System.err.println(" Erreur GET notes: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.ok(new java.util.ArrayList<>());  // Retourne HTTP 200 (OK) avec une liste vide (fallback silencieux)
        }
    }

    @PostMapping("/children/{childId}/notes")  // Associe les requêtes POST à "/api/children/{childId}/notes" à cette méthode
    public ResponseEntity<?> addNote(  // Ajoute une nouvelle note pour un enfant (réservé aux psychologues)
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL
            @RequestBody NoteRequest req,  // Extrait les données de la note (contenu, date de session) du corps JSON
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("📝 POST /children/" + childId + "/notes");  // Log de débogage avec emoji
            System.out.println("   - Content: " + req.getNote());  // Log du contenu de la note (débogage)
            System.out.println("   - SessionDate: " + req.getSessionDate());  // Log de la date de session (débogage)
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            PsychologistNote note = psychologistNoteService.addNote(childId, req, user.getUsername());  // Appelle le service pour créer la note (vérifie que l'utilisateur est psychologue)
            
            Map<String, Object> response = new HashMap<>();  // Crée une map pour la réponse JSON
            response.put("id", note.getId());  // Ajoute l'ID de la note créée
            response.put("note", note.getNote());  // Ajoute le contenu de la note
            response.put("sessionDate", note.getSessionDate());  // Ajoute la date de session
            response.put("createdAt", note.getCreatedAt());  // Ajoute la date de création
            response.put("psychologistName", note.getPsychologist().getName());  // Ajoute le nom du psychologue qui a écrit la note
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);  // Retourne HTTP 201 (Created) avec la note créée en JSON

        } catch (Exception e) {  // Capture toute exception (utilisateur non psychologue, enfant non trouvé)
            System.err.println(" Erreur POST note: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }

    @DeleteMapping("/notes/{id}")  // Associe les requêtes DELETE à "/api/notes/{id}" à cette méthode
    public ResponseEntity<?> deleteNote(  // Supprime une note spécifique (seul le psychologue auteur peut la supprimer)
            @PathVariable Long id,  // Extrait l'ID de la note depuis l'URL (ex: /notes/42)
            @AuthenticationPrincipal UserDetails user) {  // Récupère l'utilisateur authentifié depuis le token JWT
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            System.out.println("🗑️ DELETE /notes/" + id);  // Log de débogage avec emoji poubelle
            
            if (user == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non connecté"));  // Retourne HTTP 401 (Unauthorized)
            }

            psychologistNoteService.deleteNote(id, user.getUsername());  // Appelle le service pour supprimer la note (vérifie que l'utilisateur est le psychologue auteur)
            return ResponseEntity.ok(Map.of("message", "Note supprimée"));  // Retourne HTTP 200 (OK) avec un message de confirmation

        } catch (Exception e) {  // Capture toute exception (non autorisé, note non trouvée)
            System.err.println(" Erreur delete note: " + e.getMessage());  // Log d'erreur
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  // Retourne HTTP 400 (Bad Request) avec le message d'erreur
        }
    }
}