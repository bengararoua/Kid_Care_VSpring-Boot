package com.kidcare.insight.controller;  // Déclare le package contenant les contrôleurs REST de l'application

import com.kidcare.insight.dto.UserDTO;  // Importe l'objet de transfert contenant les données de l'utilisateur à envoyer au frontend (sans le mot de passe)
import com.kidcare.insight.entity.User;  // Importe l'entité représentant un utilisateur (parent, enseignant, psychologue)
import com.kidcare.insight.repository.UserRepository;  // Importe le repository JPA pour accéder aux données des utilisateurs en base
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection automatique de dépendances
import org.springframework.http.ResponseEntity;  // Importe la classe permettant de construire des réponses HTTP complètes
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // Importe l'annotation pour récupérer l'utilisateur authentifié directement (non utilisé ici)
import org.springframework.security.core.userdetails.UserDetails;  // Importe l'interface contenant les informations de l'utilisateur connecté (non utilisé ici)
import org.springframework.web.bind.annotation.*;  // Importe toutes les annotations REST (RestController, RequestMapping, GetMapping)

import java.util.List;  // Importe l'interface List pour manipuler des collections d'objets
import java.util.stream.Collectors;  // Importe la classe Collectors pour collecter les résultats des streams Java

@RestController  // Déclare cette classe comme un contrôleur REST (chaque méthode retourne directement du JSON)
@RequestMapping("/api/users")  // Définit le préfixe commun "/api/users" pour tous les endpoints de ce contrôleur
public class UserController {  // Déclare le contrôleur gérant la consultation des utilisateurs par rôle

    @Autowired  // Injecte automatiquement le bean UserRepository géré par Spring
    private UserRepository userRepository;  // Repository pour accéder aux données des utilisateurs en base de données

    @GetMapping("/psychologists")  // Associe les requêtes GET à "/api/users/psychologists" à cette méthode
    public ResponseEntity<List<UserDTO>> getPsychologists() {  // Récupère la liste de tous les utilisateurs ayant le rôle PSYCHOLOGUE
        List<User> psychologists = userRepository.findByRole("psychologist");  // Appelle le repository pour trouver tous les utilisateurs avec le rôle "psychologist"
        
        List<UserDTO> dtos = psychologists.stream()  // Convertit la liste des entités User en flux (stream)
            .map(UserDTO::new)  // Transforme chaque entité User en objet UserDTO (en utilisant le constructeur UserDTO(user))
            .collect(Collectors.toList());  // Collecte tous les DTO transformés dans une nouvelle liste
        
        return ResponseEntity.ok(dtos);  // Retourne HTTP 200 (OK) avec la liste des psychologues en JSON
    }

    @GetMapping("/teachers")  // Associe les requêtes GET à "/api/users/teachers" à cette méthode
    public ResponseEntity<List<UserDTO>> getTeachers() {  // Récupère la liste de tous les utilisateurs ayant le rôle ENSEIGNANT
        List<User> teachers = userRepository.findByRole("teacher");  // Appelle le repository pour trouver tous les utilisateurs avec le rôle "teacher"
        
        List<UserDTO> dtos = teachers.stream()  // Convertit la liste des entités User en flux (stream)
            .map(UserDTO::new)  // Transforme chaque entité User en objet UserDTO
            .collect(Collectors.toList());  // Collecte tous les DTO transformés dans une nouvelle liste
        
        return ResponseEntity.ok(dtos);  // Retourne HTTP 200 (OK) avec la liste des enseignants en JSON
    }

    @GetMapping("/parents")  // Associe les requêtes GET à "/api/users/parents" à cette méthode
    public ResponseEntity<List<UserDTO>> getParents() {  // Récupère la liste de tous les utilisateurs ayant le rôle PARENT
        List<User> parents = userRepository.findByRole("parent");  // Appelle le repository pour trouver tous les utilisateurs avec le rôle "parent"
        
        List<UserDTO> dtos = parents.stream()  // Convertit la liste des entités User en flux (stream)
            .map(UserDTO::new)  // Transforme chaque entité User en objet UserDTO
            .collect(Collectors.toList());  // Collecte tous les DTO transformés dans une nouvelle liste
        
        return ResponseEntity.ok(dtos);  // Retourne HTTP 200 (OK) avec la liste des parents en JSON
    }
}