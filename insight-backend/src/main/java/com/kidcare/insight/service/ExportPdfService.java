package com.kidcare.insight.service;  // Déclare le package contenant les services métier de l'application

import com.kidcare.insight.entity.Child;  // Importe l'entité Child pour les informations de l'enfant
import com.kidcare.insight.entity.BehaviorLog;  // Importe l'entité BehaviorLog pour les données comportementales
import com.kidcare.insight.repository.BehaviorLogRepository;  // Importe le repository pour les logs
import com.kidcare.insight.repository.ChildRepository;  // Importe le repository pour les enfants
import com.lowagie.text.*;  // Importe les classes iText pour la génération PDF (Document, Paragraph, Font, etc.)
import com.lowagie.text.pdf.PdfPTable;  // Importe la classe pour créer des tableaux PDF
import com.lowagie.text.pdf.PdfPCell;  // Importe la classe pour créer des cellules de tableau PDF
import com.lowagie.text.pdf.PdfWriter;  // Importe la classe pour écrire le PDF
import org.springframework.beans.factory.annotation.Autowired;  // Importe l'annotation pour l'injection de dépendances
import org.springframework.stereotype.Service;  // Importe l'annotation pour déclarer un service Spring

import java.io.ByteArrayOutputStream;  // Importe le flux de sortie pour stocker le PDF en mémoire
import java.time.LocalDate;  // Importe la classe LocalDate pour les dates
import java.time.format.DateTimeFormatter;  // Importe le formateur de dates
import java.util.List;  // Importe l'interface List pour les collections

@Service  // Déclare cette classe comme un service Spring (contenant la logique métier)
public class ExportPdfService {  // Déclare le service responsable de l'export des rapports PDF

    @Autowired  // Injecte automatiquement le repository ChildRepository
    private ChildRepository childRepository;  // Repository pour récupérer les informations de l'enfant
    
    @Autowired  // Injecte automatiquement le repository BehaviorLogRepository
    private BehaviorLogRepository behaviorLogRepository;  // Repository pour récupérer les logs comportementaux

    // Méthode principale pour générer un rapport PDF pour un enfant
    public byte[] generateChildReport(Long childId) throws Exception {
        System.out.println("📄 Début génération PDF pour enfant ID: " + childId);  // Log de début
        
        // Récupération de l'enfant
        Child child = childRepository.findById(childId)  // Cherche l'enfant par son ID
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé: " + childId));  // Lance exception si non trouvé
        System.out.println("✅ Enfant trouvé: " + child.getName());  // Log de confirmation
        
        // Récupération des logs comportementaux de l'enfant
        List<BehaviorLog> logs = behaviorLogRepository.findByChildIdOrderByLogDateDesc(childId);  // Récupère tous les logs triés par date
        System.out.println("✅ " + logs.size() + " logs trouvés");  // Log du nombre de logs
        
        // Création du document PDF en mémoire
        ByteArrayOutputStream out = new ByteArrayOutputStream();  // Crée un flux de sortie en mémoire (pas de fichier temporaire)
        Document document = new Document(PageSize.A4);  // Crée un document PDF au format A4
        PdfWriter.getInstance(document, out);  // Lie le document au flux de sortie
        
        document.open();  // Ouvre le document pour l'écriture
        
        // ===== DÉFINITION DES POLICES DE CARACTÈRES =====
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);  // Police pour le titre principal (gras, taille 24)
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);  // Police pour le sous-titre (normal, taille 12)
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);  // Police normale (taille 10)
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);  // Police en gras (taille 10)
        Font largeBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);  // Police grand format en gras (taille 20)
        
        // ===== TITRE PRINCIPAL =====
        Paragraph title = new Paragraph("🏠 KidCare Insight", titleFont);  // Crée un paragraphe avec le titre
        title.setAlignment(Element.ALIGN_CENTER);  // Centre le titre horizontalement
        document.add(title);  // Ajoute le titre au document
        
        // ===== SOUS-TITRE =====
        Paragraph subtitle = new Paragraph("Rapport de suivi personnalisé", subtitleFont);  // Crée le sous-titre
        subtitle.setAlignment(Element.ALIGN_CENTER);  // Centre le sous-titre
        subtitle.setSpacingAfter(10);  // Ajoute un espace de 10 points après le sous-titre
        document.add(subtitle);  // Ajoute le sous-titre au document
        
        // ===== BADGE OFFICIEL =====
        Paragraph badge = new Paragraph("📄 RAPPORT OFFICIEL 📄",   // Crée un badge "OFFICIEL"
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));  // Police en gras taille 11
        badge.setAlignment(Element.ALIGN_CENTER);  // Centre le badge
        badge.setSpacingAfter(20);  // Espace après (20 points)
        document.add(badge);  // Ajoute le badge au document
        
        // ===== LIGNE DÉCORATIVE =====
        Paragraph separator = new Paragraph("═══════════════════════════════════════════════════", normalFont);  // Ligne de séparation
        separator.setAlignment(Element.ALIGN_CENTER);  // Centre la ligne
        separator.setSpacingAfter(15);  // Espace après (15 points)
        document.add(separator);  // Ajoute la ligne au document
        
        // ===== NOM ET ÂGE DE L'ENFANT =====
        Paragraph childName = new Paragraph("👶 " + child.getName() + " • " + child.getAge() + " ans",   // Affiche le nom et l'âge
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));  // Police en gras taille 18
        childName.setAlignment(Element.ALIGN_CENTER);  // Centre le texte
        childName.setSpacingAfter(20);  // Espace après (20 points)
        document.add(childName);  // Ajoute le nom de l'enfant au document
        
        // ===== CALCUL DES STATISTIQUES =====
        double avgFocus = logs.stream()  // Parcourt tous les logs
                .mapToInt(l -> l.getFocusLevel() != null ? l.getFocusLevel() : 0)  // Extrait le niveau de concentration (0 si null)
                .average().orElse(0);  // Calcule la moyenne (0 si aucun log)
        
        // ===== TABLEAU DES STATISTIQUES GÉNÉRALES =====
        PdfPTable statsTable = new PdfPTable(2);  // Crée un tableau de 2 colonnes
        statsTable.setWidthPercentage(80);  // Définit la largeur à 80% de la page
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le tableau horizontalement
        statsTable.setSpacingAfter(20);  // Espace après le tableau (20 points)
        
        // Cellule 1 - Nombre d'observations
        PdfPCell cell1 = new PdfPCell();  // Crée une cellule
        cell1.setPadding(12);  // Définit les marges internes (12 points)
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le contenu horizontalement
        cell1.addElement(new Paragraph("📋", FontFactory.getFont(FontFactory.HELVETICA, 24)));  // Ajoute un emoji document
        cell1.addElement(new Paragraph(String.valueOf(logs.size()), largeBoldFont));  // Ajoute le nombre de logs (en grand)
        cell1.addElement(new Paragraph("OBSERVATIONS", normalFont));  // Ajoute le label "OBSERVATIONS"
        statsTable.addCell(cell1);  // Ajoute la cellule au tableau
        
        // Cellule 2 - Concentration moyenne
        PdfPCell cell2 = new PdfPCell();  // Crée une cellule
        cell2.setPadding(12);  // Définit les marges internes
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le contenu
        cell2.addElement(new Paragraph("🎯", FontFactory.getFont(FontFactory.HELVETICA, 24)));  // Ajoute un emoji cible
        cell2.addElement(new Paragraph(String.format("%.1f/5", avgFocus), largeBoldFont));  // Affiche la moyenne (ex: "3.5/5")
        cell2.addElement(new Paragraph("CONCENTRATION MOYENNE", normalFont));  // Label
        statsTable.addCell(cell2);  // Ajoute la cellule au tableau
        
        document.add(statsTable);  // Ajoute le tableau des statistiques au document
        
        // ===== INFORMATIONS DES PROFESSIONNELS (PARENT, ENSEIGNANT, PSYCHOLOGUE) =====
        PdfPTable proTable = new PdfPTable(3);  // Crée un tableau de 3 colonnes
        proTable.setWidthPercentage(90);  // Largeur à 90% de la page
        proTable.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le tableau
        proTable.setSpacingAfter(20);  // Espace après le tableau (20 points)
        
        // Récupération des noms (ou "Non assigné" si null)
        String parentName = child.getParent() != null ? child.getParent().getName() : "Non assigné";  // Nom du parent
        String teacherName = child.getTeacher() != null ? child.getTeacher().getName() : "Non assigné";  // Nom de l'enseignant
        String psychologistName = child.getPsychologist() != null ? child.getPsychologist().getName() : "Non assigné";  // Nom du psychologue
        
        // Cellule PARENT
        PdfPCell parentCell = new PdfPCell();  // Crée une cellule
        parentCell.setPadding(10);  // Marges internes
        parentCell.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le contenu
        parentCell.addElement(new Paragraph("👨‍👩‍👧", FontFactory.getFont(FontFactory.HELVETICA, 24)));  // Emoji famille
        parentCell.addElement(new Paragraph("PARENT / TUTEUR", boldFont));  // Label en gras
        parentCell.addElement(new Paragraph(parentName, normalFont));  // Nom du parent
        proTable.addCell(parentCell);  // Ajoute la cellule au tableau
        
        // Cellule ENSEIGNANT
        PdfPCell teacherCell = new PdfPCell();  // Crée une cellule
        teacherCell.setPadding(10);
        teacherCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        teacherCell.addElement(new Paragraph("📚", FontFactory.getFont(FontFactory.HELVETICA, 24)));  // Emoji livre
        teacherCell.addElement(new Paragraph("ENSEIGNANT(E)", boldFont));
        teacherCell.addElement(new Paragraph(teacherName, normalFont));
        proTable.addCell(teacherCell);
        
        // Cellule PSYCHOLOGUE
        PdfPCell psychologistCell = new PdfPCell();  // Crée une cellule
        psychologistCell.setPadding(10);
        psychologistCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        psychologistCell.addElement(new Paragraph("👩‍⚕️", FontFactory.getFont(FontFactory.HELVETICA, 24)));  // Emoji médecin
        psychologistCell.addElement(new Paragraph("PSYCHOLOGUE", boldFont));
        psychologistCell.addElement(new Paragraph(psychologistName, normalFont));
        proTable.addCell(psychologistCell);
        
        document.add(proTable);  // Ajoute le tableau des professionnels au document
        
        // ===== LIGNE DE SÉPARATION =====
        Paragraph separator2 = new Paragraph("───────────────────────────────────────────────────", normalFont);  // Ligne de séparation
        separator2.setAlignment(Element.ALIGN_CENTER);  // Centre la ligne
        separator2.setSpacingAfter(10);  // Espace après (10 points)
        document.add(separator2);  // Ajoute la ligne au document
        
        // ===== STATISTIQUES DES HUMEURS =====
        long happy = logs.stream().filter(l -> "happy".equals(l.getMood())).count();  // Compte les humeurs "happy"
        long neutral = logs.stream().filter(l -> "neutral".equals(l.getMood())).count();  // Compte les humeurs "neutral"
        long sad = logs.stream().filter(l -> "sad".equals(l.getMood()) || "angry".equals(l.getMood())).count();  // Compte "sad" ou "angry"
        
        Paragraph moodTitle = new Paragraph("📊 RÉPARTITION DES HUMEURS", boldFont);  // Titre de la section
        moodTitle.setAlignment(Element.ALIGN_CENTER);  // Centre le titre
        moodTitle.setSpacingAfter(10);  // Espace après
        document.add(moodTitle);  // Ajoute le titre
        
        PdfPTable moodTable = new PdfPTable(3);  // Crée un tableau de 3 colonnes
        moodTable.setWidthPercentage(90);  // Largeur à 90%
        moodTable.setHorizontalAlignment(Element.ALIGN_CENTER);  // Centre le tableau
        moodTable.setSpacingAfter(20);  // Espace après (20 points)
        
        // Cellule HEUREUX
        PdfPCell happyCell = new PdfPCell();
        happyCell.setPadding(10);
        happyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        happyCell.addElement(new Paragraph("😊", FontFactory.getFont(FontFactory.HELVETICA, 32)));  // Grand emoji sourire
        happyCell.addElement(new Paragraph(String.valueOf(happy), largeBoldFont));  // Nombre en grand
        happyCell.addElement(new Paragraph("Heureux / Joyeux", normalFont));
        moodTable.addCell(happyCell);
        
        // Cellule NEUTRE
        PdfPCell neutralCell = new PdfPCell();
        neutralCell.setPadding(10);
        neutralCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        neutralCell.addElement(new Paragraph("😐", FontFactory.getFont(FontFactory.HELVETICA, 32)));  // Emoji neutre
        neutralCell.addElement(new Paragraph(String.valueOf(neutral), largeBoldFont));
        neutralCell.addElement(new Paragraph("Neutre / Calme", normalFont));
        moodTable.addCell(neutralCell);
        
        // Cellule TRISTE
        PdfPCell sadCell = new PdfPCell();
        sadCell.setPadding(10);
        sadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        sadCell.addElement(new Paragraph("😢", FontFactory.getFont(FontFactory.HELVETICA, 32)));  // Emoji triste
        sadCell.addElement(new Paragraph(String.valueOf(sad), largeBoldFont));
        sadCell.addElement(new Paragraph("Triste / Fatigué", normalFont));
        moodTable.addCell(sadCell);
        
        document.add(moodTable);  // Ajoute le tableau des humeurs
        
        // ===== TABLEAU DES DERNIERS LOGS =====
        if (!logs.isEmpty()) {  // Si des logs existent
            Paragraph logsTitle = new Paragraph("📋 Dernières observations",   // Titre de la section
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));  // Police en gras taille 14
            logsTitle.setSpacingAfter(10);  // Espace après
            document.add(logsTitle);  // Ajoute le titre
            
            PdfPTable logsTable = new PdfPTable(5);  // Crée un tableau de 5 colonnes (Date, Humeur, Concentration, Sommeil, Interaction)
            logsTable.setWidthPercentage(100);  // Largeur à 100% de la page
            
            // ===== EN-TÊTES DU TABLEAU =====
            String[] headers = {"📅 Date", "😊 Humeur", "🎯 Concentration", "😴 Sommeil", "👥 Interaction"};  // Labels des colonnes
            for (String header : headers) {  // Parcourt chaque en-tête
                PdfPCell headerCell = new PdfPCell(new Paragraph(header, boldFont));  // Crée une cellule avec texte en gras
                headerCell.setPadding(8);  // Marges internes
                logsTable.addCell(headerCell);  // Ajoute la cellule au tableau
            }
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");  // Formateur de date (jour/mois/année)
            
            for (BehaviorLog log : logs) {  // Parcourt chaque log
                // Formatage de la date
                String dateStr = "";
                if (log.getLogDate() != null) {
                    if (log.getLogDate() instanceof LocalDate) {
                        dateStr = ((LocalDate) log.getLogDate()).format(dateFormatter);  // Formatte la date
                    } else {
                        dateStr = log.getLogDate().toString();  // Conversion en string
                    }
                }
                
                logsTable.addCell(dateStr);  // Ajoute la date
                logsTable.addCell(getMoodEmoji(log.getMood()) + " " + (log.getMood() != null ? log.getMood() : "N/A"));  // Ajoute l'humeur avec emoji
                logsTable.addCell((log.getFocusLevel() != null ? log.getFocusLevel() : 0) + "/5");  // Ajoute la concentration
                logsTable.addCell((log.getSleepHours() != null ? log.getSleepHours() : 0) + "h");  // Ajoute les heures de sommeil
                logsTable.addCell((log.getSocialInteraction() != null ? log.getSocialInteraction() : 0) + "/5");  // Ajoute l'interaction sociale
            }
            
            document.add(logsTable);  // Ajoute le tableau des logs au document
        } else {
            // Message si aucun log n'est disponible
            Paragraph noData = new Paragraph("📭 Aucune donnée d'observation disponible.", normalFont);  // Message
            noData.setAlignment(Element.ALIGN_CENTER);  // Centre le message
            document.add(noData);  // Ajoute le message au document
        }
        
        // ===== LIGNE DÉCORATIVE AVANT LE PIED DE PAGE =====
        Paragraph footerSeparator = new Paragraph("═══════════════════════════════════════════════════", normalFont);
        footerSeparator.setAlignment(Element.ALIGN_CENTER);
        footerSeparator.setSpacingBefore(20);  // Espace AVANT (20 points)
        document.add(footerSeparator);
        
        // ===== PIED DE PAGE =====
        Paragraph footer = new Paragraph();  // Crée un paragraphe
        footer.add(new Chunk("© 2026 KidCare Insight – Application de suivi pédagogique et psychologique\n",   // Ligne 1 : copyright
                FontFactory.getFont(FontFactory.HELVETICA, 8)));  // Police petite taille
        footer.add(new Chunk("📅 Rapport généré le " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),   // Ligne 2 : date de génération
                FontFactory.getFont(FontFactory.HELVETICA, 8)));  // Police petite taille
        footer.setAlignment(Element.ALIGN_CENTER);  // Centre le pied de page
        footer.setSpacingBefore(10);  // Espace avant (10 points)
        document.add(footer);  // Ajoute le pied de page
        
        document.close();  // Ferme le document (finalise l'écriture)
        System.out.println("✅ PDF généré avec succès, taille: " + out.size() + " bytes");  // Log de confirmation
        
        return out.toByteArray();  // Retourne le tableau d'octets du PDF
    }
    
    // Méthode privée pour obtenir l'emoji correspondant à une humeur
    private String getMoodEmoji(String mood) {
        if (mood == null) return "😐";  // Emoji neutre par défaut
        switch (mood.toLowerCase()) {  // Conversion en minuscules pour comparer
            case "happy": return "😊";  // Heureux → emoji sourire
            case "sad": return "😢";  // Triste → emoji pleur
            case "angry": return "😡";  // En colère → emoji en colère
            case "excited": return "🤩";  // Excité → étoiles dans les yeux
            case "anxious": return "😰";  // Anxieux → emoji peur
            default: return "😐";  // Par défaut → neutre
        }
    }
}