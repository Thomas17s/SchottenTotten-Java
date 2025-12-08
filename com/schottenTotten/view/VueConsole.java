package com.schottenTotten.view;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Joueur;
import com.schottenTotten.model.Couleur; 

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class VueConsole {
    // Codes couleurs
    public static final String RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[91m";
    public static final String ANSI_GREEN = "\u001B[92m";
    public static final String ANSI_YELLOW = "\u001B[93m";
    public static final String ANSI_BLUE = "\u001B[94m";
    public static final String ANSI_PURPLE = "\u001B[95m";
    public static final String ANSI_CYAN = "\u001B[96m";

    // Pattern pour supprimer les codes couleurs invisibles
    private static final Pattern ANSI_PATTERN = Pattern.compile("(?:\u001B|\033)\\[[;\\d]*m");

    private Scanner scanner;

    public VueConsole() {
        this.scanner = new Scanner(System.in);
    }

    public void afficherPlateau(List<Borne> bornes, Joueur j1, Joueur j2) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                  PLATEAU DE JEU SCHOTTEN-TOTTEN");
        System.out.println("=".repeat(70));
        
        // En-tête
        System.out.println(String.format("%-30s | %s | %30s", j1.getNom(), "BORNES", j2.getNom()));
        System.out.println("-".repeat(70));

        for (Borne borne : bornes) {
            StringBuilder sb = new StringBuilder();
           
            // Préparer le texte de gauche (J1)
            String texteJ1 = formaterCote(borne.getCoteJoueur1());
            // L'aligner proprement à GAUCHE sur 30 caractères
            sb.append(alignerGauche(texteJ1, 30));
           
            // Préparer la borne centrale
            String symbole;
            if (borne.getProprietaire() == j1) symbole = ANSI_GREEN + "< J1 " + RESET;
            else if (borne.getProprietaire() == j2) symbole = ANSI_RED + " J2 >" + RESET;
            else symbole = ANSI_YELLOW + "( " + (borne.getId() + 1) + " )" + RESET;
           
            sb.append(" | ").append(symbole).append(" | ");

            // Préparer le texte de droite (J2)
            String texteJ2 = formaterCote(borne.getCoteJoueur2());
            // L'aligner proprement à DROITE sur 30 caractères
            sb.append(alignerDroite(texteJ2, 30));
           
            System.out.println(sb.toString());
        }
        System.out.println("=".repeat(70));
    }

    // Ajoute des espaces APRES le texte pour qu'il fasse 'largeur' caractères visuels.

    private String alignerGauche(String texte, int largeur) {
        int longueurReelle = getLongueurVisible(texte);
        int espacesAajouter = largeur - longueurReelle;
        
        if (espacesAajouter <= 0) return texte; // Si ça dépasse, on ne coupe pas
        return texte + " ".repeat(espacesAajouter);
    }

    // Ajoute des espaces avant le texte pour qu'il fasse 'largeur' caractères visuels.

    private String alignerDroite(String texte, int largeur) {
        int longueurReelle = getLongueurVisible(texte);
        int espacesAajouter = largeur - longueurReelle;
        
        if (espacesAajouter <= 0) return texte;
        return " ".repeat(espacesAajouter) + texte;
    }

    // Compte les caractères visibles (ignore les codes couleurs).

    private int getLongueurVisible(String str) {
        if (str == null) return 0;
        return ANSI_PATTERN.matcher(str).replaceAll("").length();
    }

    private String formaterCote(List<Carte> cartes) {
        StringBuilder sb = new StringBuilder();
        for (Carte c : cartes) {
            sb.append(coloriser(c)).append(" ");
        }
        return sb.toString();
    }

    private String coloriser(Carte carte) {
        if (carte == null || carte.getCouleur() == null) return "[?]";
        String code = RESET;
        switch (carte.getCouleur()) {
            case Rouge: code = ANSI_RED; break;
            case Vert: code = ANSI_GREEN; break;
            case Bleu: code = ANSI_BLUE; break;
            case Jaune: code = ANSI_YELLOW; break;
            case Violet: code = ANSI_PURPLE; break;
            case Marron: code = ANSI_CYAN; break;
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