package com.schottenTotten.view;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Joueur;
import com.schottenTotten.model.Couleur; 

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class VueConsole {
    // Codes couleurs ANSI
    public static final String RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[91m";
    public static final String ANSI_GREEN = "\u001B[92m";
    public static final String ANSI_YELLOW = "\u001B[93m";
    public static final String ANSI_BLUE = "\u001B[94m";
    public static final String ANSI_PURPLE = "\u001B[95m";
    public static final String ANSI_CYAN = "\u001B[96m";
    public static final String ANSI_WHITE = "\u001B[97m";

    // Pattern pour supprimer les codes couleurs invisibles (pour l'alignement)
    private static final Pattern ANSI_PATTERN = Pattern.compile("(?:\u001B|\033)\\[[;\\d]*m");

    private Scanner scanner;

    public VueConsole() {
        this.scanner = new Scanner(System.in);
    }

    public void afficherPlateau(List<Borne> bornes, Joueur j1, Joueur j2) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                   PLATEAU DE JEU SCHOTTEN-TOTTEN");
        System.out.println("   Légende : " + ANSI_CYAN + "(? 1 ?)" + RESET + " = Colin-Maillard | " + 
                           ANSI_PURPLE + "(4c 1)" + RESET + " = Combat de Boue");
        System.out.println("=".repeat(80));
        
        // En-tête
        System.out.println(String.format("%-30s | %s | %30s", j1.getNom(), " BORNES  ", j2.getNom()));
        System.out.println("-".repeat(80));

        for (Borne borne : bornes) {
            StringBuilder sb = new StringBuilder();
           
            // 1. Préparer le texte de gauche (J1)
            String texteJ1 = formaterCote(borne.getCoteJoueur1());
            sb.append(alignerGauche(texteJ1, 30));
           
            // 2. Préparer la borne centrale (Largeur fixe de 9 caractères visibles)
            String symbole;
            String num = String.valueOf(borne.getId() + 1);

            if (borne.getProprietaire() == j1) {
                // Largeur 9 : " <  J1   "
                symbole = ANSI_GREEN + " <  J1   " + RESET;
            } 
            else if (borne.getProprietaire() == j2) {
                // Largeur 9 : "   J2  > "
                symbole = ANSI_RED + "   J2  > " + RESET;
            } 
            else {
                if (borne.isColinMaillard()) {
                    // Largeur 9 : "(?  1  ?)"
                    symbole = ANSI_CYAN + "(?  " + num + "  ?)" + RESET;
                } else if (borne.getCapaciteMax() == 4) {
                    // Largeur 9 : "( 4c 1  )"
                    symbole = ANSI_PURPLE + "( 4c " + num + "  )" + RESET;
                } else {
                    // Largeur 9 : "(   1   )"
                    symbole = ANSI_YELLOW + "(   " + num + "   )" + RESET;
                }
            }
           
            sb.append(" | ").append(symbole).append(" | ");

            // 3. Préparer le texte de droite (J2)
            String texteJ2 = formaterCote(borne.getCoteJoueur2());
            sb.append(alignerDroite(texteJ2, 30));
           
            System.out.println(sb.toString());
        }
        System.out.println("=".repeat(80));
    }

    // --- MÉTHODES D'ALIGNEMENT ---

    private String alignerGauche(String texte, int largeur) {
        int longueurReelle = getLongueurVisible(texte);
        int espacesAajouter = largeur - longueurReelle;
        
        if (espacesAajouter <= 0) return texte; 
        return texte + " ".repeat(espacesAajouter);
    }

    private String alignerDroite(String texte, int largeur) {
        int longueurReelle = getLongueurVisible(texte);
        int espacesAajouter = largeur - longueurReelle;
        
        if (espacesAajouter <= 0) return texte;
        return " ".repeat(espacesAajouter) + texte;
    }

    private int getLongueurVisible(String str) {
        if (str == null) return 0;
        return ANSI_PATTERN.matcher(str).replaceAll("").length();
    }

    // --- FORMATAGE ET COULEURS ---

    private String formaterCote(List<Carte> cartes) {
        StringBuilder sb = new StringBuilder();
        for (Carte c : cartes) {
            sb.append(coloriser(c)).append(" ");
        }
        return sb.toString();
    }

    private String coloriser(Carte carte) {
        if (carte == null) return "[?]";
        
        // Si carte tactique sans couleur (Ruse non jouée ou spéciale)
        if (carte.getCouleur() == null) {
            return ANSI_WHITE + "[" + carte.toString() + "]" + RESET;
        }
        
        String code = RESET;
        switch (carte.getCouleur()) {
            case Rouge:  code = ANSI_RED; break;
            case Vert:   code = ANSI_GREEN; break;
            case Bleu:   code = ANSI_BLUE; break;
            case Jaune:  code = ANSI_YELLOW; break;
            case Violet: code = ANSI_PURPLE; break;
            case Marron: code = ANSI_CYAN; break; 
            default:     code = RESET;
        }
        return code + "[" + carte.toString() + "]" + RESET;
    }

    public void afficherMain(Joueur p) {
        System.out.println("\nMain de " + p.getNom() + " :");
        for (int i = 0; i < p.getMain().size(); i++) {
            System.out.print(i + ":" + coloriser(p.getMain().get(i)) + "  ");
        }
        System.out.println();
    }

    public void afficherMessage(String msg) {
        System.out.println(">> " + msg);
    }

    public int demanderEntier(String message, int min, int max) {
        int input = -1;
        while (true) {
            System.out.print(message + " (" + min + "-" + max + ") : ");
            try {
                String line = scanner.nextLine();
                input = Integer.parseInt(line);
                if (input >= min && input <= max) break;
            } catch (NumberFormatException e) { }
            System.out.println("Entrée invalide.");
        }
        return input;
    }
}