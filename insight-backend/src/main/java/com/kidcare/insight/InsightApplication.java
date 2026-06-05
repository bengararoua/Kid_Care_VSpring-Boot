package com.kidcare.insight;  // Déclare le package racine de l'application

import org.springframework.boot.SpringApplication;  // Importe la classe SpringApplication pour lancer l'application Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication;  // Importe l'annotation @SpringBootApplication qui active la configuration automatique
import org.springframework.scheduling.annotation.EnableScheduling;  // Importe l'annotation @EnableScheduling pour activer les tâches planifiées

@SpringBootApplication  // Annotation principale qui combine @Configuration, @EnableAutoConfiguration et @ComponentScan
@EnableScheduling  // Active l'exécution des tâches planifiées (ex: @Scheduled dans AppointmentService)
public class InsightApplication {  // Classe principale de l'application Spring Boot

    public static void main(String[] args) {  // Point d'entrée de l'application
        SpringApplication.run(InsightApplication.class, args);  // Lance l'application Spring Boot
    }
}