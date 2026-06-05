package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import com.kidcare.insight.service.ExportPdfService;  // Importe le service contenant la logique de génération de PDF
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.HttpHeaders;  // Importe la classe permettant de construire les en-têtes HTTP (Content-Type, Content-Disposition, etc.)
import org.springframework.http.MediaType;  // Importe les types MIME standards (APPLICATION_PDF, APPLICATION_JSON, etc.)
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes (status + headers + body)
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping, PathVariable)

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON ou des données binaires)
@RequestMapping("/api/export")  // Définit le préfixe commun "/api/export" pour tous les endpoints de ce contrôleur
public class ExportController {  // Déclare le contrôleur gérant l'export des données (PDF, rapports)

    @Autowired  // Injecte automatiquement le bean ExportPdfService géré par Spring
    private ExportPdfService exportPdfService;  // Service qui contient la logique de génération des documents PDF

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @GetMapping("/child/{childId}")  // Associe les requêtes GET à "/api/export/child/{childId}" à cette méthode
    public ResponseEntity<byte[]> exportChildReport(  // Exporte un rapport PDF pour un enfant spécifique (retourne des bytes binaires)
            @PathVariable Long childId,  // Extrait l'ID de l'enfant depuis l'URL (ex: /child/5)
            @AuthenticationPrincipal UserDetails userDetails) {  // Récupère l'utilisateur authentifié depuis le token JWT
        
        try {  // Bloc try-catch pour capturer et gérer les erreurs
            // Vérifier l'authentification
            if (userDetails == null) {  // Vérifie si aucun utilisateur n'est authentifié
                return ResponseEntity.status(401).build();  // Retourne HTTP 401 (Unauthorized) sans body
            }
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())  // Cherche l'utilisateur en base par son email
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));  // Lance une exception si l'utilisateur n'existe pas
            
            // Générer le PDF
            byte[] pdfBytes = exportPdfService.generateChildReport(childId);  // Appelle le service pour générer le rapport PDF de l'enfant (vérifie les droits à l'intérieur)
            
            // Configurer les headers pour le PDF
            HttpHeaders headers = new HttpHeaders();  // Crée un objet pour les en-têtes HTTP
            headers.setContentType(MediaType.APPLICATION_PDF);  // Définit le type MIME comme "application/pdf" (indique au navigateur que c'est un PDF)
            headers.setContentDispositionFormData("attachment", "rapport_enfant_" + childId + ".pdf");  // Définit l'en-tête Content-Disposition pour forcer le téléchargement (attachment) et propose le nom du fichier
            headers.setContentLength(pdfBytes.length);  // Définit la taille du fichier PDF en bytes (pour la barre de progression du téléchargement)
            
            return ResponseEntity.ok()  // Construit une réponse HTTP 200 (OK)
                    .headers(headers)  // Ajoute les en-têtes configurés (Content-Type, Content-Disposition, Content-Length)
                    .body(pdfBytes);  // Ajoute le contenu binaire du PDF dans le corps de la réponse
                    
        } catch (Exception e) {  // Capture toute exception survenant pendant le traitement (erreur de génération, enfant non trouvé, etc.)
            e.printStackTrace();  // Affiche la trace complète de l'erreur dans les logs serveur pour débogage
            return ResponseEntity.internalServerError().build();  // Retourne HTTP 500 (Internal Server Error) sans body
        }
    }
}